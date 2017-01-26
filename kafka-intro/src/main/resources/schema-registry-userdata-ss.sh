#!/bin/bash 

# AMI Specific variables (may have to be updated as the BASE AMI evolves)
export JDK=/install/jdk1.8.0_101

# Region specific info (CI_ASSETS_BUCKET_NAME, CI_DOMAIN_NAME, TIER_NAME)
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
echo "PRIVATE_IP=$PRIVATE_IP"

echo "download the schema-registries.properties file from S3"
aws s3 cp s3://${CI_ASSETS_BUCKET_NAME}/${S3_KAFKA_ASSETS_PATH}/schema-registry-ss.properties $KAFKA_ROOT/etc/schema-registry/schema-registry.properties
sed -i "s^{CI_DOMAIN_NAME}^${CI_DOMAIN_NAME}^g" $KAFKA_ROOT/etc/schema-registry/schema-registry.properties
sed -i "s^{TIER_NAME}^${TIER_NAME}^g" $KAFKA_ROOT/etc/schema-registry/schema-registry.properties
sed -i "s^{PRIVATE_IP}^${PRIVATE_IP}^g" $KAFKA_ROOT/etc/schema-registry/schema-registry.properties


# setup the java process to use Java 8 and a large heap, and enable remote JMX access
# these variables all feed the confluent bin/kafka-run-class shell script
export JAVA_HOME=$JDK
export SCHEMA_REGISTRY_HEAP_OPTS=-Xmx2g
export JMX_PORT=5999
export JMXAUTH=false
export JMXSSL=false
export JMXLOCALONLY=false
# -Dcom.sun.management.jmxremote (enables remote JMX connections)
# -Dcom.sun.management.jmxremote.port=portNum (sets the port ... added if JMX_PORT is set)
export SCHEMA_REGISTRY_JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=$JMXAUTH -Dcom.sun.management.jmxremote.rmi.port=$JMX_PORT \
-Dcom.sun.management.jmxremote.ssl=$JMXAUTH -Dcom.sun.management.jmxremote.ssl=$JMXSSL -Dcom.sun.management.jmxremote.local.only=$JMXLOCALONLY"
echo "JAVA_HOME=$JDK"
echo "SCHEMA_REGISTRY_HEAP_OPTS=$SCHEMA_REGISTRY_HEAP_OPTS"
echo "SCHEMA_REGISTRY_JMX_OPTS=$SCHEMA_REGISTRY_JMX_OPTS"
echo "JMX_PORT=$JMX_PORT"
echo "JMXAUTH=$JMXAUTH"
echo "JMXSSL=$JMXSSL"

# Change ownership to sassrv and run as the sassrv user 
chown -R sassrv:sas $KAFKA_ROOT

echo "Starting Schema Registry as a service"
rm -f /etc/init/schema-registry.conf

echo "##############################################################" >> /etc/init/schema-registry.conf
echo "# put this in /etc/init to start zookeeper as a service" >> /etc/init/schema-registry.conf
echo "# you can use the following commands" >> /etc/init/schema-registry.conf
echo "# initctl start schema-registry" >> /etc/init/schema-registry.conf
echo "# initctl stop schema-registry" >> /etc/init/schema-registry.conf
echo "# initctl status schema-registry" >> /etc/init/schema-registry.conf
echo "##############################################################" >> /etc/init/schema-registry.conf
echo "" >> /etc/init/schema-registry.conf
echo "description \"Schema Registry Server\"" >> /etc/init/schema-registry.conf
echo "author      \"Randy Zingle\"" >> /etc/init/schema-registry.conf
echo "" >> /etc/init/schema-registry.conf
echo "start on kafka" >> /etc/init/schema-registry.conf
echo "stop on runlevel [06]" >> /etc/init/schema-registry.conf
echo "" >> /etc/init/schema-registry.conf
echo "script" >> /etc/init/schema-registry.conf
echo "" >> /etc/init/schema-registry.conf
echo "    export KAFKA_ROOT=/install/citng/kafka" >> /etc/init/schema-registry.conf
echo "    export JDK=/install/jdk1.8.0_101" >> /etc/init/schema-registry.conf
echo "    export JAVA_HOME=/install/jdk1.8.0_101" >> /etc/init/schema-registry.conf
echo "    export SCHEMA_REGISTRY_HEAP_OPTS=${SCHEMA_REGISTRY_HEAP_OPTS}" >> /etc/init/schema-registry.conf
echo "    export JMX_PORT=${JMX_PORT}" >> /etc/init/schema-registry.conf
echo "    export JMXLOCALONLY=false" >> /etc/init/schema-registry.conf
echo "    export JMXAUTH=false" >> /etc/init/schema-registry.conf
echo "    export JMXSSL=false" >> /etc/init/schema-registry.conf
echo "    export SCHEMA_REGISTRY_JMX_OPTS='${SCHEMA_REGISTRY_JMX_OPTS}'" >> /etc/init/schema-registry.conf
echo "" >> /etc/init/schema-registry.conf
echo "    exec su -m sassrv -c '${KAFKA_ROOT}/bin/schema-registry-start ${KAFKA_ROOT}/etc/schema-registry/schema-registry.properties >> /install/registry-upstart.log'" >> /etc/init/schema-registry.conf
echo "" >> /etc/init/schema-registry.conf
echo "end script" >> /etc/init/schema-registry.conf
echo "" >> /etc/init/schema-registry.conf
echo "respawn" >> /etc/init/schema-registry.conf
echo "respawn limit 15 5" >> /etc/init/schema-registry.conf

# Fire up the Schema Registry as an upstart service
initctl start schema-registry


