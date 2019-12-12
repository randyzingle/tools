#!/bin/bash

# Region and broker specific info (CI_ASSETS_BUCKET_NAME, CI_DOMAIN_NAME, TIER_NAME, LOG_RETENTION_HOURS)
source /install/install_vars.sh

export PRIVATE_IP=`curl http://169.254.169.254/latest/meta-data/local-ipv4`
echo "env variables from install_vars.sh"
echo "CI_DOMAIN_NAME=$CI_DOMAIN_NAME"
echo "CI_ASSETS_BUCKET_NAME=$CI_ASSETS_BUCKET_NAME"
echo "S3_KAFKA_ASSETS_PATH=$S3_KAFKA_ASSETS_PATH"
echo "TIER_NAME=$TIER_NAME"
echo "AVAILABILITY_ZONE=$AVAILABILITY_ZONE"

# install docker
yum update -y
amazon-linux-extras install docker -y
systemctl enable docker.service
service docker start
usermod -a -G docker ec2-user
# verify you can hit docker as the ecs-user
docker info

# install kubeadmin, kubelet, and kubectl
aws s3 cp s3://${CI_ASSETS_BUCKET_NAME}/${S3_KAFKA_ASSETS_PATH}/kubernetes.repo /etc/yum.repos.d/kubernetes.repo
# Set SELinux in permissive mode (effectively disabling it) - allows containers to access the host file system
setenforce 0
sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
yum install -y kubelet kubeadm kubectl --disableexcludes=kubernetes
systemctl enable --now kubelet

# make sure iptables is not being bypassed
cat <<EOF >  /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
sysctl --system
