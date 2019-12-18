# Setting up an EKS cluster

eksctl create cluster \
--name finnr-fargate \
--version 1.14 \
--region us-west-2 \
--fargate



eksctl create cluster \
--name finnr-ec2 \
--version 1.14 \
--region us-east-1 \
--nodegroup-name finnr-workers \
--node-type t3.medium \
--nodes 1 \
--nodes-min 1 \
--nodes-max 2 \
--managed
