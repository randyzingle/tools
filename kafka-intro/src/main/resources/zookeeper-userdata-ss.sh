#!/bin/bash 

# Zookeeper Version
# Confluent
export ZOOKEEPER_PACKAGE=confluent-latest.tar.gz
# Pure Zookeeper
#export ZOOKEEPER_PACKAGE=zookeeper-3.4.9.tar.gz

# AMI Specific variables (may have to be updated as the BASE AMI evolves)
export JDK=/install/jdk1.8.0_101

# Region specific info (CI_ASSETS_BUCKET_NAME, CI_DOMAIN_NAME, TIER_NAME)
source /install/install_vars.sh 

# Copy the confluent zookeeper code and configuration file from S3
export KAFKA_ROOT=/install/citng/kafka
mkdir $KAFKA_ROOT
echo "set environment variables:"
echo "KAFKA_ROOT=$KAFKA_ROOT"
echo "JDK=$JDK"
echo "CI_DOMAIN_NAME=$CI_DOMAIN_NAME"
echo "CI_ASSETS_BUCKET_NAME=$CI_ASSETS_BUCKET_NAME"
echo "S3_KAFKA_ASSETS_PATH=$S3_KAFKA_ASSETS_PATH"
echo "TIER_NAME=$TIER_NAME"

echo "download the confluent package from S3"
aws s3 cp s3://${CI_ASSETS_BUCKET_NAME}/${S3_KAFKA_ASSETS_PATH}/$ZOOKEEPER_PACKAGE /tmp
tar -xzf /tmp/$ZOOKEEPER_PACKAGE -C $KAFKA_ROOT/ --strip-components 1

echo "download the zookeeper.properties file from S3"
# Confluent
export CONFLUENT=etc/kafka
aws s3 cp s3://${CI_ASSETS_BUCKET_NAME}/${S3_KAFKA_ASSETS_PATH}/zookeeper-ss.properties $KAFKA_ROOT/$CONFLUENT/zookeeper.properties
sed -i "s^{CI_DOMAIN_NAME}^${CI_DOMAIN_NAME}^g" $KAFKA_ROOT/$CONFLUENT/zookeeper.properties
sed -i "s^{TIER_NAME}^${TIER_NAME}^g" $KAFKA_ROOT/$CONFLUENT/zookeeper.properties

# The node should be tagged with the zookeeper id, use this to create the myid file
INSTANCE_ID="`wget -qO- http://instance-data/latest/meta-data/instance-id`"
TAG_NAME="ZookeeperID"
REGION="`wget -qO- http://instance-data/latest/meta-data/placement/availability-zone | sed -e 's:\([0-9][0-9]*\)[a-z]*\$:\\1:'`"
ZOOKEEPER_ID="`aws ec2 describe-tags --filters "Name=resource-id,Values=$INSTANCE_ID" "Name=key,Values=$TAG_NAME" --region $REGION --output=text | cut -f5`"

echo $ZOOKEEPER_ID > /zookeeper/data/myid
sed -i "s^{ZOOKEEPER_ID}^${ZOOKEEPER_ID}^g" $KAFKA_ROOT/$CONFLUENT/zookeeper.properties

# Change ownership to sassrv and run as the sassrv user (need to upstart this)
chown -R sassrv:sas /zookeeper
chown -R sassrv:sas $KAFKA_ROOT

# Setup the Upstart file to kick things off
echo "Starting Zookeeper as a service"
rm -f /etc/init/zookeeper.conf

echo "##############################################################" >> /etc/init/zookeeper.conf
echo "# put this in /etc/init to start zookeeper as a service" >> /etc/init/zookeeper.conf
echo "# you can use the following commands" >> /etc/init/zookeeper.conf
echo "# initctl start zookeeper" >> /etc/init/zookeeper.conf
echo "# initctl stop zookeeper" >> /etc/init/zookeeper.conf
echo "# initctl status zookeeper" >> /etc/init/zookeeper.conf
echo "##############################################################" >> /etc/init/zookeeper.conf
echo "" >> /etc/init/zookeeper.conf
echo "description \"Zookeeper Server\"" >> /etc/init/zookeeper.conf
echo "author      \"Randy Zingle\"" >> /etc/init/zookeeper.conf
echo "" >> /etc/init/zookeeper.conf
echo "start on runlevel [2345]" >> /etc/init/zookeeper.conf
echo "stop on runlevel [06]" >> /etc/init/zookeeper.conf
echo "" >> /etc/init/zookeeper.conf
echo "script" >> /etc/init/zookeeper.conf
echo "" >> /etc/init/zookeeper.conf
echo "    export KAFKA_ROOT=/install/citng/kafka" >> /etc/init/zookeeper.conf
echo "    export JDK=/install/jdk1.8.0_101" >> /etc/init/zookeeper.conf
echo "    export JAVA_HOME=/install/jdk1.8.0_101" >> /etc/init/zookeeper.conf
echo "    export KAFKA_HEAP_OPTS=-Xmx13g" >> /etc/init/zookeeper.conf
echo "    export JMX_PORT=3999" >> /etc/init/zookeeper.conf
echo "    export JMXLOCALONLY=false" >> /etc/init/zookeeper.conf
echo "    export JMXAUTH=false" >> /etc/init/zookeeper.conf
echo "    export JMXSSL=false" >> /etc/init/zookeeper.conf
echo "    export KAFKA_JMX_OPTS=\"-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=\$JMXAUTH -Dcom.sun.management.jmxremote.ssl=\$JMXSSL \\" >> /etc/init/zookeeper.conf
echo "-Dcom.sun.management.jmxremote.rmi.port=\$JMX_PORT  -Dcom.sun.management.jmxremote.local.only=\$JMXLOCALONLY\"" >> /etc/init/zookeeper.conf
echo "" >> /etc/init/zookeeper.conf
echo "    exec su -m sassrv -c '\$KAFKA_ROOT/bin/zookeeper-server-start \$KAFKA_ROOT/etc/kafka/zookeeper.properties'" >> /etc/init/zookeeper.conf
echo "" >> /etc/init/zookeeper.conf
echo "end script" >> /etc/init/zookeeper.conf
echo "" >> /etc/init/zookeeper.conf
echo "respawn" >> /etc/init/zookeeper.conf
echo "respawn limit 15 5" >> /etc/init/zookeeper.conf

# Fire up Zookeeper as an upstart service
initctl start zookeeper

