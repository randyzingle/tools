#!/bin/bash

# AMI Specific variables (may have to be updated as the BASE AMI evolves)
export JDK=/install/jdk1.8.0_101

# Region and broker specific info (CI_ASSETS_BUCKET_NAME, CI_DOMAIN_NAME, TIER_NAME, LOG_RETENTION_HOURS)
source /install/install_vars.sh 

# Copy the confluent zookeeper code and configuration file from S3
export KAFKA_ROOT=/install/citng/kafka

export PRIVATE_IP=`curl http://169.254.169.254/latest/meta-data/local-ipv4`
echo "set environment variables:"
echo "KAFKA_ROOT=$KAFKA_ROOT"
echo "JDK=$JDK"
echo "CI_DOMAIN_NAME=$CI_DOMAIN_NAME"
echo "CI_ASSETS_BUCKET_NAME=$CI_ASSETS_BUCKET_NAME"
echo "S3_KAFKA_ASSETS_PATH=$S3_KAFKA_ASSETS_PATH"
echo "TIER_NAME=$TIER_NAME"
echo "LOG_RETENTION_HOURS=$LOG_RETENTION_HOURS"
echo "KAFKA_HEAP=$KAFKA_HEAP"
echo "AVAILABILITY_ZONE=$AVAILABILITY_ZONE"

echo "download the server.properties file from S3"
aws s3 cp s3://${CI_ASSETS_BUCKET_NAME}/${S3_KAFKA_ASSETS_PATH}/kafka-ss.properties $KAFKA_ROOT/etc/kafka/server.properties
# Set the Domain Name, and Tier name to create the Zookeeper suite lookup
sed -i "s^{CI_DOMAIN_NAME}^${CI_DOMAIN_NAME}^g" $KAFKA_ROOT/etc/kafka/server.properties
sed -i "s^{TIER_NAME}^${TIER_NAME}^g" $KAFKA_ROOT/etc/kafka/server.properties
# Set the BrokerID, Private IP address, and log retention time
INSTANCE_ID="`wget -qO- http://instance-data/latest/meta-data/instance-id`"
TAG_NAME="BrokerID"
REGION="`wget -qO- http://instance-data/latest/meta-data/placement/availability-zone | sed -e 's:\([0-9][0-9]*\)[a-z]*\$:\\1:'`"
BROKER_ID="`aws ec2 describe-tags --filters "Name=resource-id,Values=$INSTANCE_ID" "Name=key,Values=$TAG_NAME" --region $REGION --output=text | cut -f5`"
sed -i "s^{BROKER_ID}^${BROKER_ID}^g" $KAFKA_ROOT/etc/kafka/server.properties
sed -i "s^{PRIVATE_IP}^${PRIVATE_IP}^g" $KAFKA_ROOT/etc/kafka/server.properties
sed -i "s^{LOG_RETENTION_HOURS}^${LOG_RETENTION_HOURS}^g" $KAFKA_ROOT/etc/kafka/server.properties
sed -i "s^{AVAILABILITY_ZONE}^${AVAILABILITY_ZONE}^g" $KAFKA_ROOT/etc/kafka/server.properties

# Change ownership to sassrv and run as the sassrv user 
chown -R sassrv:sas /kafka-logs
chown -R sassrv:sas $KAFKA_ROOT

# Setup the Upstart file for Kafka
echo "Starting Kafka as a service"
rm -f /etc/init/kafka.conf

echo "##############################################################" >> /etc/init/kafka.conf
echo "# put this in /etc/init to start the kafka broker as a service" >> /etc/init/kafka.conf
echo "# you can use the following commands" >> /etc/init/kafka.conf
echo "# initctl start kafka" >> /etc/init/kafka.conf
echo "# initctl stop kafka" >> /etc/init/kafka.conf
echo "# initctl status kafka" >> /etc/init/kafka.conf
echo "##############################################################" >> /etc/init/kafka.conf
echo "" >> /etc/init/kafka.conf
echo "description \"Kafka Broker\"" >> /etc/init/kafka.conf
echo "author      \"Randy Zingle\"" >> /etc/init/kafka.conf
echo "" >> /etc/init/kafka.conf
echo "start on zookeeper" >> /etc/init/kafka.conf
echo "stop on runlevel [06]" >> /etc/init/kafka.conf
echo "" >> /etc/init/kafka.conf
echo "script" >> /etc/init/kafka.conf
echo "" >> /etc/init/kafka.conf
echo "    export KAFKA_ROOT=/install/citng/kafka" >> /etc/init/kafka.conf
echo "    export JDK=/install/jdk1.8.0_101" >> /etc/init/kafka.conf
echo "    export JAVA_HOME=/install/jdk1.8.0_101" >> /etc/init/kafka.conf
echo "    export KAFKA_HEAP_OPTS=-Xmx${KAFKA_HEAP}g" >> /etc/init/kafka.conf
echo "    export JMX_PORT=4999" >> /etc/init/kafka.conf
echo "    export JMXLOCALONLY=false" >> /etc/init/kafka.conf
echo "    export JMXAUTH=false" >> /etc/init/kafka.conf
echo "    export JMXSSL=false" >> /etc/init/kafka.conf
echo "    export KAFKA_JMX_OPTS=\"-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=\$JMXAUTH -Dcom.sun.management.jmxremote.ssl=\$JMXSSL \\" >> /etc/init/kafka.conf
echo "-Dcom.sun.management.jmxremote.rmi.port=\$JMX_PORT  -Dcom.sun.management.jmxremote.local.only=\$JMXLOCALONLY\"" >> /etc/init/kafka.conf
echo "" >> /etc/init/kafka.conf
echo "    exec su -m sassrv -c '\$KAFKA_ROOT/bin/kafka-server-start \$KAFKA_ROOT/etc/kafka/server.properties'" >> /etc/init/kafka.conf
echo "" >> /etc/init/kafka.conf
echo "end script" >> /etc/init/kafka.conf
echo "" >> /etc/init/kafka.conf
echo "respawn" >> /etc/init/kafka.conf
echo "respawn limit 15 5" >> /etc/init/kafka.conf

# Fire up Kafka as an upstart service
initctl start kafka



