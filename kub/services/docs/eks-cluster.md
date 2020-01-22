# Setting up an EKS cluster

## Cluster Creation

We'll create a cluster with three worker node group types:
* an unmanaged node group which runs 75% of its workload on spot instances, labeled with *target=stateless*
* an eks managed node group which runs all servers in the us-east-1b AZ so that we can attach EBS volumes to stateful services, labeled with *target=stateful*
* four fargate profiles which run apps in fargate using the namespaces dev, tst, prod, eurc, labeled with *target=fargate*

The cluster configuration file looks like this:

```yaml
$ cat baldur-eks.yaml
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: mymir-eks
  region: us-east-1
  version: "1.14"

vpc:
  subnets:
    private:
      us-east-1b: { id: subnet-00ecb52b }
      us-east-1c: { id: subnet-ea91809d }
    public:
      us-east-1b: { id: subnet-07ecb52c}
      us-east-1c: { id: subnet-ec91809b}

nodeGroups:
  - name: ng-stateless
    labels: { target: stateless }
    minSize: 2
    maxSize: 6
    instancesDistribution: # 25% on demand and 75% spot instances (Spot Fleet)
        maxPrice: 0.015
        instanceTypes: ["t3.small", "t3.medium", "t3a.small", "t3a.medium"]
        onDemandBaseCapacity: 0
        onDemandPercentageAboveBaseCapacity: 25
        spotInstancePools: 4
    privateNetworking: true
    ssh: # use existing EC2 key
      publicKeyName: ci-k8s
    tags:
      # EC2 tags required for cluster-autoscaler auto-discovery
      k8s.io/cluster-autoscaler/enabled: "true"
      k8s.io/cluster-autoscaler/mymir-eks: "owned"
    iam:
      withAddonPolicies:
        albIngress: true
        autoScaler: true
        cloudWatch: true

managedNodeGroups:
  - name: ng-stateful
    instanceType: t3.medium
    labels: { target: stateful }
    minSize: 2
    maxSize: 5
    volumeSize: 50
    availabilityZones: ["us-east-1b"] # lock this node group to one AZ for shared EBS
    ssh:
      publicKeyName: ci-k8s
    iam:
      withAddonPolicies:
        albIngress: true
        autoScaler: true
        cloudWatch: true
        ebs: true
        efs: true

fargateProfiles:
  - name: fp-dev
    selectors:
      # All workloads in the "dev" Kubernetes namespace matching the following
      # label selectors will be scheduled onto Fargate:
      - namespace: dev
        labels: { target: fargate }
  - name: fp-prod
    selectors:
      # All workloads in the "dev" Kubernetes namespace matching the following
      # label selectors will be scheduled onto Fargate:
      - namespace: prod
        labels: { target: fargate }
  - name: fp-tst
    selectors:
      # All workloads in the "dev" Kubernetes namespace matching the following
      # label selectors will be scheduled onto Fargate:
      - namespace: tst
        labels: { target: fargate }
  - name: fp-eurc
    selectors:
      # All workloads in the "dev" Kubernetes namespace matching the following
      # label selectors will be scheduled onto Fargate:
      - namespace: eurc
        labels: { target: fargate }
```

We create the cluster by running:

```sh
$ eksctl create cluster -f baldur-eks.yaml
```

Once cluster creation completes we can access it using *kubectl*

```sh
# check out the clusters we can hit
$ kubectl config get-clusters
NAME
mymir-eks.us-east-1.eksctl.io
arn:aws:eks:us-east-1:952478859445:cluster/baldur-eks
minikube
# see what cluster we have as our default
$ kubectl config current-context
Randall.Zingle@sas.com@mymir-eks.us-east-1.eksctl.io
# check out the servers running as worker nodes in our cluster
$ kubectl get nodes -l target=stateless
NAME                           STATUS   ROLES    AGE   VERSION
ip-10-240-11-19.ec2.internal   Ready    <none>   43m   v1.14.8-eks-b8860f
ip-10-240-27-69.ec2.internal   Ready    <none>   43m   v1.14.8-eks-b8860f
$ kubectl get nodes -l target=stateful
NAME                           STATUS   ROLES    AGE   VERSION
ip-10-240-0-19.ec2.internal    Ready    <none>   44m   v1.14.7-eks-1861c5
ip-10-240-2-195.ec2.internal   Ready    <none>   44m   v1.14.7-eks-1861c5
```

## Storage
In our managed nodegroup we've added IAM policies that allow all the access we need to work with EBS and EFS volumes.

### Install the (Container Storage Interface) CSI drivers

By default the eksctl tool deploys a cluster with a built-in storage class provisioner named *kubernetes.io/aws-ebs*.
We can see it with the following command:
```sh
$ kubectl get storageclass
NAME            PROVISIONER             AGE
gp2 (default)   kubernetes.io/aws-ebs   3d19h
```
We can use this to attach EBS volumes to our Pods. But this is being deprecated in favor of using CSI drivers so we'll create a CSI provisioner for EBS and for EFS.

#### EBS CSI Driver
```sh
# install the EBS CSI driver
$ kubectl apply -k "github.com/kubernetes-sigs/aws-ebs-csi-driver/deploy/kubernetes/overlays/stable/?ref=master"
serviceaccount/ebs-csi-controller-sa created
clusterrole.rbac.authorization.k8s.io/ebs-external-attacher-role created
clusterrole.rbac.authorization.k8s.io/ebs-external-provisioner-role created
clusterrolebinding.rbac.authorization.k8s.io/ebs-csi-attacher-binding created
clusterrolebinding.rbac.authorization.k8s.io/ebs-csi-provisioner-binding created
deployment.apps/ebs-csi-controller created
daemonset.apps/ebs-csi-node created
csidriver.storage.k8s.io/ebs.csi.aws.com created

# create a storageclass that uses the EBS CSI driver
$ cat ebs-gp2-storageclass.yaml
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: ebs-gp2-ext4
provisioner: ebs.csi.aws.com
volumeBindingMode: WaitForFirstConsumer
reclaimPolicy: Delete
allowVolumeExpansion: true
parameters:
  type: gp2
  fsType: ext4
$ kubectl apply -f ebs-gp2-storageclass.yaml
storageclass.storage.k8s.io/ebs-gp2-ext4 created

# now we have two ways to provision EBS drives:
$ kubectl get sc
NAME            PROVISIONER             AGE
ebs-gp2-ext4    ebs.csi.aws.com         51s
gp2 (default)   kubernetes.io/aws-ebs   3d19h
```

#### EFS CSI Driver
```sh
# install the EFS CSI driver
$ kubectl apply -k "github.com/kubernetes-sigs/aws-efs-csi-driver/deploy/kubernetes/overlays/stable/?ref=master"
daemonset.apps/efs-csi-node created
csidriver.storage.k8s.io/efs.csi.aws.com created

# create a storageclass that uses the EFS CSI driver
$ cat efs-storageclass.yaml
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: efs-sc
provisioner: efs.csi.aws.com
reclaimPolicy: Retain
$ kubectl apply -f efs-storageclass.
storageclass.storage.k8s.io/efs-sc created

# Now we have a way to provision EFS drives:
$ kubectl get sc
NAME            PROVISIONER             AGE
ebs-gp2-ext4    ebs.csi.aws.com         9m39s
efs-sc          efs.csi.aws.com         30s
gp2 (default)   kubernetes.io/aws-ebs   3d19h
```

## Cluster Scaling

```sh
$ kubectl get nodes -l target=stateless
NAME                            STATUS   ROLES    AGE    VERSION
ip-10-240-10-103.ec2.internal   Ready    <none>   124m   v1.14.8-eks-b8860f
ip-10-240-24-210.ec2.internal   Ready    <none>   124m   v1.14.8-eks-b8860f


$ cat cluster-scaling-testapp.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: baldur-deployment
spec:
  selector:
    matchLabels:
      app: baldur
  replicas: 2
  template:
    metadata:
      labels:
        app: baldur
    spec:
        containers:
        - name: baldur-ts
          image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
          imagePullPolicy: Always
          resources:
            requests:
              memory: "512Mi"
              cpu: "500m"
            limits:
              memory: "1024Mi"
              cpu: "1000m"
        nodeSelector:
          target: stateless
$ kubectl apply -f cluster-scaling-testapp.yaml
deployment.apps/baldur-deployment created

# Now we have 2 pods running on our stateless nodes (see *kubectl get nodes -l target=stateless* above)
$ kubectl get pods -o wide
NAME                                 READY   STATUS    RESTARTS   AGE     IP              NODE                            NOMINATED NODE   READINESS GATES
baldur-deployment-6967bf65bb-qqv6f   1/1     Running   0          3m59s   10.240.28.209   ip-10-240-24-210.ec2.internal   <none>           <none>
baldur-deployment-6967bf65bb-xtcqb   1/1     Running   0          3m59s   10.240.12.187   ip-10-240-10-103.ec2.internal   <none>           <none>

# Let's scale this out
$ kubectl get deployments
NAME                READY   UP-TO-DATE   AVAILABLE   AGE
baldur-deployment   2/2     2            2           55m
$ kubectl scale deployment baldur-deployment --replicas=10

# after a while...
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-6967bf65bb-47nts   0/1     Pending   0          2m
baldur-deployment-6967bf65bb-6w665   1/1     Running   0          2m
baldur-deployment-6967bf65bb-bg54k   1/1     Running   0          2m
baldur-deployment-6967bf65bb-c592r   0/1     Pending   0          2m
baldur-deployment-6967bf65bb-fc2g7   0/1     Pending   0          2m
baldur-deployment-6967bf65bb-fdpj5   0/1     Pending   0          2m
baldur-deployment-6967bf65bb-gz8jp   1/1     Running   0          2m
baldur-deployment-6967bf65bb-qqv6f   1/1     Running   0          57m
baldur-deployment-6967bf65bb-xtcqb   1/1     Running   0          57m
baldur-deployment-6967bf65bb-zgj8l   1/1     Running   0          2m

# our stateless node group is made of 2 t3.small instances which each have 2vCPUs and 2GiB of RAM
# we have only 6 out of 10 pods running because each server only has resources enough for 3 pods
```

### Deploy the Cluster Autoscaler

```sh
# get the cluster autoscaler yaml file:
$ wget https://raw.githubusercontent.com/kubernetes/autoscaler/master/cluster-autoscaler/cloudprovider/aws/examples/cluster-autoscaler-autodiscover.yaml

# edit the above file
# add this annotation to the cluster-autoscaler deployment:
cluster-autoscaler.kubernetes.io/safe-to-evict: 'false'

# edit the cluster-autoscaler container command to
# replace:
<YOUR CLUSTER NAME> with your cluster name
# add options:
--balance-similar-node-groups
--skip-nodes-with-system-pods=false
# make sure the image matches your kubernetes version:
image: k8s.gcr.io/cluster-autoscaler:v1.14.7
```

We should now have this:
```sh
$ cat cluster-autoscaler-autodiscover.yaml
---
apiVersion: v1
kind: ServiceAccount
metadata:
  labels:
    k8s-addon: cluster-autoscaler.addons.k8s.io
    k8s-app: cluster-autoscaler
  name: cluster-autoscaler
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: cluster-autoscaler
  labels:
    k8s-addon: cluster-autoscaler.addons.k8s.io
    k8s-app: cluster-autoscaler
rules:
  - apiGroups: [""]
    resources: ["events", "endpoints"]
    verbs: ["create", "patch"]
  - apiGroups: [""]
    resources: ["pods/eviction"]
    verbs: ["create"]
  - apiGroups: [""]
    resources: ["pods/status"]
    verbs: ["update"]
  - apiGroups: [""]
    resources: ["endpoints"]
    resourceNames: ["cluster-autoscaler"]
    verbs: ["get", "update"]
  - apiGroups: [""]
    resources: ["nodes"]
    verbs: ["watch", "list", "get", "update"]
  - apiGroups: [""]
    resources:
      - "pods"
      - "services"
      - "replicationcontrollers"
      - "persistentvolumeclaims"
      - "persistentvolumes"
    verbs: ["watch", "list", "get"]
  - apiGroups: ["extensions"]
    resources: ["replicasets", "daemonsets"]
    verbs: ["watch", "list", "get"]
  - apiGroups: ["policy"]
    resources: ["poddisruptionbudgets"]
    verbs: ["watch", "list"]
  - apiGroups: ["apps"]
    resources: ["statefulsets", "replicasets", "daemonsets"]
    verbs: ["watch", "list", "get"]
  - apiGroups: ["storage.k8s.io"]
    resources: ["storageclasses", "csinodes"]
    verbs: ["watch", "list", "get"]
  - apiGroups: ["batch", "extensions"]
    resources: ["jobs"]
    verbs: ["get", "list", "watch", "patch"]
  - apiGroups: ["coordination.k8s.io"]
    resources: ["leases"]
    verbs: ["create"]
  - apiGroups: ["coordination.k8s.io"]
    resourceNames: ["cluster-autoscaler"]
    resources: ["leases"]
    verbs: ["get", "update"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: cluster-autoscaler
  namespace: kube-system
  labels:
    k8s-addon: cluster-autoscaler.addons.k8s.io
    k8s-app: cluster-autoscaler
rules:
  - apiGroups: [""]
    resources: ["configmaps"]
    verbs: ["create","list","watch"]
  - apiGroups: [""]
    resources: ["configmaps"]
    resourceNames: ["cluster-autoscaler-status", "cluster-autoscaler-priority-expander"]
    verbs: ["delete", "get", "update", "watch"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: cluster-autoscaler
  labels:
    k8s-addon: cluster-autoscaler.addons.k8s.io
    k8s-app: cluster-autoscaler
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-autoscaler
subjects:
  - kind: ServiceAccount
    name: cluster-autoscaler
    namespace: kube-system

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: cluster-autoscaler
  namespace: kube-system
  labels:
    k8s-addon: cluster-autoscaler.addons.k8s.io
    k8s-app: cluster-autoscaler
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: cluster-autoscaler
subjects:
  - kind: ServiceAccount
    name: cluster-autoscaler
    namespace: kube-system

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cluster-autoscaler
  namespace: kube-system
  labels:
    app: cluster-autoscaler
spec:
  replicas: 1
  selector:
    matchLabels:
      app: cluster-autoscaler
  template:
    metadata:
      labels:
        app: cluster-autoscaler
      annotations:
        prometheus.io/scrape: 'true'
        prometheus.io/port: '8085'
        cluster-autoscaler.kubernetes.io/safe-to-evict: 'false'
    spec:
      serviceAccountName: cluster-autoscaler
      containers:
        - image: k8s.gcr.io/cluster-autoscaler:v1.14.7
          name: cluster-autoscaler
          resources:
            limits:
              cpu: 100m
              memory: 300Mi
            requests:
              cpu: 100m
              memory: 300Mi
          command:
            - ./cluster-autoscaler
            - --v=4
            - --stderrthreshold=info
            - --cloud-provider=aws
            - --skip-nodes-with-local-storage=false
            - --expander=least-waste
            - --node-group-auto-discovery=asg:tag=k8s.io/cluster-autoscaler/enabled,k8s.io/cluster-autoscaler/mymir-eks
            - --balance-similar-node-groups
            - --skip-nodes-with-system-pods=false
          volumeMounts:
            - name: ssl-certs
              mountPath: /etc/ssl/certs/ca-certificates.crt
              readOnly: true
          imagePullPolicy: "Always"
      volumes:
        - name: ssl-certs
          hostPath:
            path: "/etc/ssl/certs/ca-bundle.crt"
```

Let's install it:
```sh
$ kubectl apply -f cluster-autoscaler-autodiscover.yaml
serviceaccount/cluster-autoscaler created
clusterrole.rbac.authorization.k8s.io/cluster-autoscaler created
role.rbac.authorization.k8s.io/cluster-autoscaler created
clusterrolebinding.rbac.authorization.k8s.io/cluster-autoscaler created
rolebinding.rbac.authorization.k8s.io/cluster-autoscaler created
deployment.apps/cluster-autoscaler created
```

And roll out our 10 Pod app again (same steps as above):
```sh
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-6967bf65bb-6mlxl   1/1     Running   0          7m4s
baldur-deployment-6967bf65bb-9cm2d   1/1     Running   0          7m4s
baldur-deployment-6967bf65bb-9zh9r   1/1     Running   0          7m26s
baldur-deployment-6967bf65bb-bxdvz   1/1     Running   0          7m4s
baldur-deployment-6967bf65bb-fbdhp   1/1     Running   0          7m4s
baldur-deployment-6967bf65bb-g5h9g   1/1     Running   0          7m4s
baldur-deployment-6967bf65bb-lh2lb   1/1     Running   0          3m29s
baldur-deployment-6967bf65bb-q8q4c   1/1     Running   0          3m29s
baldur-deployment-6967bf65bb-spdg4   1/1     Running   0          3m29s
baldur-deployment-6967bf65bb-vhvtl   1/1     Running   0          7m26s

$ kubectl get nodes -l target=stateless
NAME                            STATUS   ROLES    AGE     VERSION
ip-10-240-10-103.ec2.internal   Ready    <none>   4h1m    v1.14.8-eks-b8860f
ip-10-240-11-114.ec2.internal   Ready    <none>   7m16s   v1.14.8-eks-b8860f
ip-10-240-24-210.ec2.internal   Ready    <none>   4h1m    v1.14.8-eks-b8860f
ip-10-240-27-225.ec2.internal   Ready    <none>   5m45s   v1.14.8-eks-b8860f
```
Nice, our autoscaler scaled out 2 more servers and all the pods have been deployed.

Scale back down:
```sh
$ kubectl scale deployment baldur-deployment --replicas=2
deployment.extensions/baldur-deployment scaled
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-6967bf65bb-9zh9r   1/1     Running   0          10m
baldur-deployment-6967bf65bb-vhvtl   1/1     Running   0          10m
$ kubectl get nodes -l target=stateless
NAME                            STATUS   ROLES    AGE     VERSION
ip-10-240-10-103.ec2.internal   Ready    <none>   4h3m    v1.14.8-eks-b8860f
ip-10-240-11-114.ec2.internal   Ready    <none>   9m23s   v1.14.8-eks-b8860f
ip-10-240-24-210.ec2.internal   Ready    <none>   4h3m    v1.14.8-eks-b8860f
ip-10-240-27-225.ec2.internal   Ready    <none>   7m52s   v1.14.8-eks-b8860f
```

Check out the autoscaler's logs:
```sh
$ kubectl -n kube-system logs -f deployment.apps/cluster-autoscaler
...
I0114 17:59:46.280560       1 static_autoscaler.go:303] Filtering out schedulables
I0114 17:59:46.280654       1 static_autoscaler.go:320] No schedulable pods
I0114 17:59:46.280673       1 static_autoscaler.go:328] No unschedulable pods
I0114 17:59:46.280686       1 static_autoscaler.go:375] Calculating unneeded nodes
I0114 17:59:46.280720       1 utils.go:583] Skipping ip-10-240-0-171.ec2.internal - node group min size reached
I0114 17:59:46.280732       1 utils.go:583] Skipping ip-10-240-4-179.ec2.internal - node group min size reached
I0114 17:59:46.280794       1 scale_down.go:410] Node ip-10-240-11-114.ec2.internal - utilization 0.055000
I0114 17:59:46.280813       1 scale_down.go:410] Node ip-10-240-27-225.ec2.internal - utilization 0.055000
I0114 17:59:46.280822       1 scale_down.go:410] Node ip-10-240-10-103.ec2.internal - utilization 0.405000
I0114 17:59:46.280832       1 scale_down.go:410] Node ip-10-240-24-210.ec2.internal - utilization 0.305000
I0114 17:59:46.280958       1 cluster.go:90] Fast evaluation: ip-10-240-10-103.ec2.internal for removal
I0114 17:59:46.281010       1 cluster.go:225] Pod default/baldur-deployment-6967bf65bb-vhvtl can be moved to ip-10-240-27-225.ec2.internal
I0114 17:59:46.281056       1 cluster.go:225] Pod kube-system/coredns-56678dcf76-nmkv7 can be moved to ip-10-240-27-225.ec2.internal
I0114 17:59:46.281095       1 cluster.go:225] Pod kube-system/coredns-56678dcf76-h29f9 can be moved to ip-10-240-27-225.ec2.internal
I0114 17:59:46.281113       1 cluster.go:121] Fast evaluation: node ip-10-240-10-103.ec2.internal may be removed
I0114 17:59:46.281122       1 cluster.go:90] Fast evaluation: ip-10-240-24-210.ec2.internal for removal
I0114 17:59:46.281147       1 cluster.go:225] Pod default/baldur-deployment-6967bf65bb-9zh9r can be moved to ip-10-240-27-225.ec2.internal
I0114 17:59:46.281177       1 cluster.go:225] Pod kube-system/ebs-csi-controller-8579f977f4-pff4p can be moved to ip-10-240-27-225.ec2.internal
I0114 17:59:46.281192       1 cluster.go:121] Fast evaluation: node ip-10-240-24-210.ec2.internal may be removed
I0114 17:59:46.281237       1 static_autoscaler.go:391] ip-10-240-10-103.ec2.internal is unneeded since 2020-01-14 17:56:45.203663395 +0000 UTC m=+29.940659198 duration 3m1.076158432s
I0114 17:59:46.281253       1 static_autoscaler.go:391] ip-10-240-24-210.ec2.internal is unneeded since 2020-01-14 17:56:45.203663395 +0000 UTC m=+29.940659198 duration 3m1.076158432s
I0114 17:59:46.281263       1 static_autoscaler.go:391] ip-10-240-11-114.ec2.internal is unneeded since 2020-01-14 17:56:45.203663395 +0000 UTC m=+29.940659198 duration 3m1.076158432s
I0114 17:59:46.281273       1 static_autoscaler.go:391] ip-10-240-27-225.ec2.internal is unneeded since 2020-01-14 17:56:45.203663395 +0000 UTC m=+29.940659198 duration 3m1.076158432s
...
```

We can see in the log that two of our nodes are scheduled for removal. If we wait (15 min or so) we see that we're back down to 2 nodes:
```sh
$ kubectl get nodes -l target=stateless
NAME                            STATUS   ROLES    AGE     VERSION
ip-10-240-10-103.ec2.internal   Ready    <none>   4h33m   v1.14.8-eks-b8860f
ip-10-240-24-210.ec2.internal   Ready    <none>   4h33m   v1.14.8-eks-b8860f
```

## Horizontal Pod Autoscaler (HPA)

In the previous section we scaled out a deployment manually with *kubectl scale deployment baldur-deployment --replicas=10*.
We really want our services to scale out automatically if they become resource constrained, and then scale back in when the load drops.
We do this by setting up metrics that will trigger scale in/out and creating a HorizontalPodAutoscaler resource for our deployment.

### Metric Server
First we have to setup the metric server to use as an aggregator of resource usage data for our cluster:

```sh
# Grab the code
$ DOWNLOAD_URL=$(curl -Ls "https://api.github.com/repos/kubernetes-sigs/metrics-server/releases/latest" | jq -r .tarball_url)
$ DOWNLOAD_VERSION=$(grep -o '[^/v]*$' <<< $DOWNLOAD_URL)
$ curl -Ls $DOWNLOAD_URL -o metrics-server-$DOWNLOAD_VERSION.tar.gz
$ mkdir metrics-server-$DOWNLOAD_VERSION
$ tar -xzf metrics-server-$DOWNLOAD_VERSION.tar.gz --directory metrics-server-$DOWNLOAD_VERSION --strip-components 1

# install the metrics server
$ kubectl apply -f metrics-server-$DOWNLOAD_VERSION/deploy/1.8+/
clusterrole.rbac.authorization.k8s.io/system:aggregated-metrics-reader created
clusterrolebinding.rbac.authorization.k8s.io/metrics-server:system:auth-delegator created
rolebinding.rbac.authorization.k8s.io/metrics-server-auth-reader created
apiservice.apiregistration.k8s.io/v1beta1.metrics.k8s.io created
serviceaccount/metrics-server created
deployment.apps/metrics-server created
service/metrics-server created
clusterrole.rbac.authorization.k8s.io/system:metrics-server created
clusterrolebinding.rbac.authorization.k8s.io/system:metrics-server created

# Now we have a metric server:
$ kubectl -n kube-system get deployment/metrics-server
NAME             READY   UP-TO-DATE   AVAILABLE   AGE
metrics-server   1/1     1            1           33s
```

Let's point an HPA at our *baldur-deployment*:
```sh
$ cat baldur-deployment-hpa.yaml
apiVersion: autoscaling/v2beta2
kind: HorizontalPodAutoscaler
metadata:
  name: baldur-autoscaler
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: baldur-deployment
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 50
$ kubectl apply -f baldur-deployment-hpa.yaml
horizontalpodautoscaler.autoscaling/baldur-autoscaler created
```
We now have a deployment with 2 pods that is scheduled to scale out if CPU usages exceeds 50%.
```sh
$ kubectl get deployments
NAME                READY   UP-TO-DATE   AVAILABLE   AGE
baldur-deployment   2/2     2            2           47m
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-6967bf65bb-9zh9r   1/1     Running   0          47m
baldur-deployment-6967bf65bb-vhvtl   1/1     Running   0          47m
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   0%/50%    2         10        2          47s
```
We're currently sitting at 0% CPU usages (the 0 under the TARGETS column - the 50 is metric target). Let's ramp up CPU usage on one of our pods:
```sh
$ kubectl port-forward baldur-deployment-6967bf65bb-9zh9r 8888:8080
Forwarding from 127.0.0.1:8888 -> 8080
Forwarding from [::1]:8888 -> 8080
$ curl http://localhost:8888/tenantSplitter/internals/cpuload
["driving cpu usage"]

# we're above the target CPU usage now:
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   88%/50%   2         10        2          6m9s

# our HPA is spinning up new pods:
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-6967bf65bb-9zh9r   1/1     Running   0          53m
baldur-deployment-6967bf65bb-h4zgb   1/1     Running   0          37s
baldur-deployment-6967bf65bb-mmdbf   1/1     Running   0          37s
baldur-deployment-6967bf65bb-vhvtl   1/1     Running   0          53m
```

Now lets drop the load on the CPU
```sh
$ curl http://localhost:8888/tenantSplitter/internals/cpuunload
["dropping cpu usage"]

# Check our deployment (we've scaled all the way up to 9 pods now)
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   0%/50%    2         10        9          9m51s
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-6967bf65bb-9zh9r   1/1     Running   0          57m
baldur-deployment-6967bf65bb-h4zgb   1/1     Running   0          3m54s
baldur-deployment-6967bf65bb-khkpx   1/1     Running   0          2m38s
baldur-deployment-6967bf65bb-mmdbf   1/1     Running   0          3m54s
baldur-deployment-6967bf65bb-qx8p7   1/1     Running   0          2m53s
baldur-deployment-6967bf65bb-v2kq2   1/1     Running   0          2m53s
baldur-deployment-6967bf65bb-vhvtl   1/1     Running   0          57m
baldur-deployment-6967bf65bb-wfhb6   1/1     Running   0          2m53s
baldur-deployment-6967bf65bb-z22tt   1/1     Running   0          2m53s

# Wait 10-15 mins, and you can we we're back down to 2 pods
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   0%/50%    2         10        2          20m
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-6967bf65bb-9zh9r   1/1     Running   0          68m
baldur-deployment-6967bf65bb-vhvtl   1/1     Running   0          68m
```

## Helm
Helm is a widely used tool to install packages in kubernetes.
The [https://helm.sh/](helm) website has plenty of documentation on how to install and use Helm.

Some base commands:
```sh
# get the version (we're using Helm 3)
$ helm version
version.BuildInfo{Version:"v3.0.1", GitCommit:"7c22ef9ce89e0ebeb7125ba2ebf7d421f3e82ffa", GitTreeState:"clean", GoVersion:"go1.13.4"}
repo (add, list, remove, update, and index chart repositories)
list (list releases)
install
uninstall
show
search
# list the repos that we're setup to pull charts from
$ helm repo list
NAME  	URL
stable	https://kubernetes-charts.storage.googleapis.com/

# search the Helm Hub for a project
$ helm search hub prometheus
# search you're configured repos for a project
$ helm search repo prometheus

# add a repo
$ helm repo add brigade https://brigadecore.github.io/charts
"brigade" has been added to your repositories
$ helm repo list
NAME   	URL
stable 	https://kubernetes-charts.storage.googleapis.com/
brigade	https://brigadecore.github.io/charts

# list projects in our new repo
$ helm search repo brigade
NAME                        	CHART VERSION	APP VERSION	DESCRIPTION
brigade/brigade             	1.4.3        	v1.2.1     	Brigade provides event-driven scripting of Kube...
brigade/brigade-github-app  	0.5.1        	v0.2.1     	The Brigade GitHub App, an advanced gateway for...
brigade/brigade-github-oauth	0.3.0        	v0.20.0    	The legacy OAuth GitHub Gateway for Brigade
brigade/brigade-k8s-gateway 	0.3.0        	           	A Helm chart for Kubernetes
brigade/brigade-project     	1.0.0        	v1.0.0     	Create a Brigade project
brigade/kashti              	0.5.0        	v0.4.0     	A Helm chart for Kubernetes

# show all info about a chart
$ helm show all brigade/kashti

# install a chart: helm install [your-petname-for-release] [chartversion/name]
$ helm install baldur brigade/kashti
NAME: baldur
LAST DEPLOYED: Wed Jan 15 09:31:05 2020
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
1. Get the application URL by running these commands:
  To connect to the Kashti dashboard, use execute brig dashboard and access it on http://localhost:8081 (you can provide the --port <local-port> flag to specify another local port)

# list all of our helm-installed applications
$ helm list
NAME  	NAMESPACE	REVISION	UPDATED                             	STATUS  	CHART       	APP VERSION
baldur	default  	1       	2020-01-15 09:31:05.167399 -0500 EST	deployed	kashti-0.5.0	v0.4.0

# unistall the app
$ helm uninstall baldur
release "baldur" uninstalled
$ helm list
NAME	NAMESPACE	REVISION	UPDATED	STATUS	CHART	APP VERSION
```

## Prometheus
[https://prometheus.io/](Prometheus) is a time series database that we can use to store our cluster and application metric data.

We'll use helm to install it and our EBS CSI driver to provide permanent EBS storage.

```sh
# lookup our storageclass name
$ kubectl get sc
NAME            PROVISIONER             AGE
ebs-gp2-ext4    ebs.csi.aws.com         23h
efs-sc          efs.csi.aws.com         23h
gp2 (default)   kubernetes.io/aws-ebs   4d18h

# create a prometheus namespace for our installation
$ kubectl create namespace prometheus

# install using our EBS CSI driver, the prometheus namespace
$ helm install prometheus stable/prometheus \
    --namespace prometheus \
    --set alertmanager.persistentVolume.storageClass="ebs-gp2-ext4",server.persistentVolume.storageClass="ebs-gp2-ext4"
NAME: prometheus
LAST DEPLOYED: Wed Jan 15 09:52:40 2020
NAMESPACE: prometheus
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
The Prometheus server can be accessed via port 80 on the following DNS name from within your cluster:
prometheus-server.prometheus.svc.cluster.local


Get the Prometheus server URL by running these commands in the same shell:
  export POD_NAME=$(kubectl get pods --namespace prometheus -l "app=prometheus,component=server" -o jsonpath="{.items[0].metadata.name}")
  kubectl --namespace prometheus port-forward $POD_NAME 9090


The Prometheus alertmanager can be accessed via port 80 on the following DNS name from within your cluster:
prometheus-alertmanager.prometheus.svc.cluster.local


Get the Alertmanager URL by running these commands in the same shell:
  export POD_NAME=$(kubectl get pods --namespace prometheus -l "app=prometheus,component=alertmanager" -o jsonpath="{.items[0].metadata.name}")
  kubectl --namespace prometheus port-forward $POD_NAME 9093
#################################################################################
######   WARNING: Pod Security Policy has been moved to a global property.  #####
######            use .Values.podSecurityPolicy.enabled with pod-based      #####
######            annotations                                               #####
######            (e.g. .Values.nodeExporter.podSecurityPolicy.annotations) #####
#################################################################################


The Prometheus PushGateway can be accessed via port 9091 on the following DNS name from within your cluster:
prometheus-pushgateway.prometheus.svc.cluster.local


Get the PushGateway URL by running these commands in the same shell:
  export POD_NAME=$(kubectl get pods --namespace prometheus -l "app=prometheus,component=pushgateway" -o jsonpath="{.items[0].metadata.name}")
  kubectl --namespace prometheus port-forward $POD_NAME 9091

For more information on running Prometheus, visit:
https://prometheus.io/
```
Let's see what we installed:
```sh
$ kubectl get pods -n prometheus
NAME                                             READY   STATUS    RESTARTS   AGE
prometheus-alertmanager-657dc59d9c-dgnsf         2/2     Running   0          11m
prometheus-kube-state-metrics-6d98644bcd-7rjn6   1/1     Running   0          11m
prometheus-node-exporter-6526h                   1/1     Running   0          11m
prometheus-node-exporter-lb9gq                   1/1     Running   0          11m
prometheus-node-exporter-lhzb8                   1/1     Running   0          11m
prometheus-node-exporter-rpxsf                   1/1     Running   0          11m
prometheus-pushgateway-cbc8d6c9b-rk5jw           1/1     Running   0          11m
prometheus-server-757dfdd677-97zrq               2/2     Running   0          11m
```
We have four DaemonSet pods installed (the prometheus-node-exporter-xxx pods), one one each worker node. These grab kubernetes metrics from each pod and export them to the prometheus server.

Our server needs persistent storage which means we have to bring it up on one of our stateful worker nodes. We can see below that kubernetes was smart enough to see that, and installed the server on our stateful node ip-10-240-4-141.ec2.internal. These nodes have the EBS IAM roles (set on cluster creation) and the stateless nodes do not **VERIFY THIS WORKS EVERY TIME**.
```sh
$ kubectl get pods -n prometheus -o wide
NAME                                             READY   STATUS    RESTARTS   AGE   IP              NODE                            NOMINATED NODE   READINESS GATES
prometheus-alertmanager-657dc59d9c-dgnsf         2/2     Running   0          18m   10.240.4.74     ip-10-240-4-141.ec2.internal    <none>           <none>
prometheus-kube-state-metrics-6d98644bcd-7rjn6   1/1     Running   0          18m   10.240.6.161    ip-10-240-4-141.ec2.internal    <none>           <none>
prometheus-node-exporter-6526h                   1/1     Running   0          18m   10.240.1.230    ip-10-240-1-230.ec2.internal    <none>           <none>
prometheus-node-exporter-lb9gq                   1/1     Running   0          18m   10.240.4.141    ip-10-240-4-141.ec2.internal    <none>           <none>
prometheus-node-exporter-lhzb8                   1/1     Running   0          18m   10.240.12.181   ip-10-240-12-181.ec2.internal   <none>           <none>
prometheus-node-exporter-rpxsf                   1/1     Running   0          18m   10.240.27.148   ip-10-240-27-148.ec2.internal   <none>           <none>
prometheus-pushgateway-cbc8d6c9b-rk5jw           1/1     Running   0          18m   10.240.7.167    ip-10-240-4-141.ec2.internal    <none>           <none>
prometheus-server-757dfdd677-97zrq               2/2     Running   0          18m   10.240.1.140    ip-10-240-4-141.ec2.internal    <none>           <none>
$ kubectl get nodes -l target=stateful
NAME                           STATUS   ROLES    AGE   VERSION
ip-10-240-1-230.ec2.internal   Ready    <none>   54m   v1.14.7-eks-1861c5
ip-10-240-4-141.ec2.internal   Ready    <none>   54m   v1.14.7-eks-1861c5
```

If we look at the mounted EBS volumes we can see that both the AlertManager and the Server have created persistent volumes:
```sh
# Persistent Volumes (our EBS disks)
$ kubectl get pv -n prometheus
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                                STORAGECLASS   REASON   AGE
pvc-ae38cf36-37a6-11ea-93a5-0a78255967a1   8Gi        RWO            Delete           Bound    prometheus/prometheus-server         ebs-gp2-ext4            25m
pvc-ae38cf5b-37a6-11ea-93a5-0a78255967a1   2Gi        RWO            Delete           Bound    prometheus/prometheus-alertmanager   ebs-gp2-ext4            25m

# Pod claim for the disk (disk is attached to the pod)
$ kubectl get pvc -n prometheus
NAME                      STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
prometheus-alertmanager   Bound    pvc-ae38cf5b-37a6-11ea-93a5-0a78255967a1   2Gi        RWO            ebs-gp2-ext4   25m
prometheus-server         Bound    pvc-ae38cf36-37a6-11ea-93a5-0a78255967a1   8Gi        RWO            ebs-gp2-ext4   25m
```

Let's check it out:
```sh
# Look at the Prometheus server:
$ export POD_NAME=$(kubectl get pods --namespace prometheus -l "app=prometheus,component=server" -o jsonpath="{.items[0].metadata.name}")
$ kubectl -n prometheus port-forward $POD_NAME 9090
# now pull up http://localhost:9090 in a browser ... go to Status->Targets and you'll see Prometheus is scraping 2 API servers and our 4 worker nodes
```

#### Grafana
Let's use grafana to visualize our custom metrics:

```sh
$ helm install grafana-baldur stable/grafana
# delete this with: $ helm delete grafana-baldur
```

### Built-in Metrics
If we go to the main page we can see a ton of metrics available in the drop down that we can examine via the console or graph view.
Our baldur-deployment pods are available since we left that running.

Let's select one of the Pods and checkout how the CPU load is tracked when we hit the cpuload endpoint as we did in the HPA section.

Use the query: container_cpu_usage_seconds_total{pod="baldur-deployment-65bd74fc58-bq64v"} - and we can see (manually refreshing the graph) that the CPU usage spikes.

There are actually two graphs that show the CPU spike. One is for the Pod and the other is for the container within the Pod. These are tracked separately because pods can have multiple containers. If we just wanted to see the container we could use *container_cpu_usage_seconds_total{pod="baldur-deployment-65bd74fc58-bq64v",container_name="baldur-ts"}*.

We could grab all the pods in the deployment with *container_cpu_usage_seconds_total{pod=~"baldur-deployment.+"}*., and we would see that the one Pod remained flat in CPU usage while the one that we loaded up spiked.

How many persistent volumes are we using: *sum(kube_persistentvolume_info)*.

### Custom Metrics
This is great for generic metrics like CPU usage and memory, but what about our custom metrics? None of the show up as available metrics in Prometheus.
This is because our baldur-deployment Pods have not been configured to export their metrics to Prometheus. We will modify our Pod description with annotations that tell Prometheus, that we should be scraped, and what to scrape:

```sh
# add the following annotations to our Pod description:
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/path: /tenantSplitter/prometheus/metrics
    prometheus.io/port: "8080"
# and redeploy the app
```
The metrics supplied at the */tenantSplitter/prometheus/metrics* endpoint, must be in the [Prometheus format](https://prometheus.io/docs/concepts/data_model). If you check out the metric drop down on the main Prometheus page you can now select the *baldur_dog_treats_eaten* metric, which is a custom metric being pulled from our baldur-deployment pods.

### Scaling on custom metrics: Prometheus Adapter

Now we have our custom metrics being pulled from our Pods and stored in Prometheus. We need to make these available at a metrics endpoint that the HPA hits so that we can scale based on their values. We can use the prometheus-adapter to pull custom metrics from Prometheus and expose them at the endpoint */apis/custom.metrics.k8s.io/v1beta1*, which is watched by the HPA.

#### Installing the prometheus-adapter
we need to install the prometheus-adapter chart and supply it:
- prometheus url
- prometheus port
- list of custom rules (which metrics to pull from prometheus and expose at /apis/custom.metrics.k8s.io/v1beta1)

The Prometheus server URL, from within the cluster is it's simple service DNS name. We can check it out by logging into one of our containers and hitting it with curl:

bash-4.4$ curl http://prometheus-server.prometheus.svc.cluster.local

Run the Helm chart to install the adapter.

```sh
$ helm install prometheus-adaptor stable/prometheus-adapter \
    --namespace prometheus \
    --set prometheus.url="http://prometheus-server.prometheus.svc.cluster.local",prometheus.port="80"
NAME: prometheus-adaptor
LAST DEPLOYED: Thu Jan 16 16:19:51 2020
NAMESPACE: prometheus
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
prometheus-adaptor-prometheus-adapter has been deployed.

# In a few minutes you should be able to list custom metrics using the following command(s):
$ kubectl get --raw /apis/custom.metrics.k8s.io/v1beta1 | jq
```

When you run the above command you'll notice (if you dig through the morass of data), that only a subset of the Prometheus metrics have been pushed to the endpoint - a subset that doesn't include our custom metrics. We need to change the configuration of the prometheus-adaptor so that it includes our metrics. We do this by changing it's config map, where the metric selection rules are stored:

```sh
$ kubectl -n prometheus get configmaps
NAME                                    DATA   AGE
prometheus-adaptor-prometheus-adapter   1      4d18h
prometheus-alertmanager                 1      6d1h
prometheus-server                       5      6d1h
```
The nicely named prometheus-adaptor-prometheus-adaptor config map is what we're looking for. If we look at the details we see the metric selection rules:

```
$ kubectl -n prometheus describe configmap prometheus-adaptor-prometheus-adapter
Name:         prometheus-adaptor-prometheus-adapter
Namespace:    prometheus
Labels:       app=prometheus-adapter
              chart=prometheus-adapter-1.4.0
              heritage=Helm
              release=prometheus-adaptor
Annotations:  <none>

Data
====
config.yaml:
----
rules:
- seriesQuery: '{__name__=~"^container_.*",container_name!="POD",namespace!="",pod_name!=""}'
  seriesFilters: []
  resources:
    overrides:
      namespace:
        resource: namespace
      pod_name:
        resource: pod
  name:
    matches: ^container_(.*)_seconds_total$
    as: ""
  metricsQuery: sum(rate(<<.Series>>{<<.LabelMatchers>>,container_name!="POD"}[5m]))
    by (<<.GroupBy>>)
- seriesQuery: '{__name__=~"^container_.*",container_name!="POD",namespace!="",pod_name!=""}'
  seriesFilters:
  - isNot: ^container_.*_seconds_total$
  resources:
    overrides:
      namespace:
        resource: namespace
      pod_name:
        resource: pod
  name:
    matches: ^container_(.*)_total$
    as: ""
  metricsQuery: sum(rate(<<.Series>>{<<.LabelMatchers>>,container_name!="POD"}[5m]))
    by (<<.GroupBy>>)
- seriesQuery: '{__name__=~"^container_.*",container_name!="POD",namespace!="",pod_name!=""}'
  seriesFilters:
  - isNot: ^container_.*_total$
  resources:
    overrides:
      namespace:
        resource: namespace
      pod_name:
        resource: pod
  name:
    matches: ^container_(.*)$
    as: ""
  metricsQuery: sum(<<.Series>>{<<.LabelMatchers>>,container_name!="POD"}) by (<<.GroupBy>>)
- seriesQuery: '{namespace!="",__name__!~"^container_.*"}'
  seriesFilters:
  - isNot: .*_total$
  resources:
    template: <<.Resource>>
  name:
    matches: ""
    as: ""
  metricsQuery: sum(<<.Series>>{<<.LabelMatchers>>}) by (<<.GroupBy>>)
- seriesQuery: '{namespace!="",__name__!~"^container_.*"}'
  seriesFilters:
  - isNot: .*_seconds_total
  resources:
    template: <<.Resource>>
  name:
    matches: ^(.*)_total$
    as: ""
  metricsQuery: sum(rate(<<.Series>>{<<.LabelMatchers>>}[5m])) by (<<.GroupBy>>)
- seriesQuery: '{namespace!="",__name__!~"^container_.*"}'
  seriesFilters: []
  resources:
    template: <<.Resource>>
  name:
    matches: ^(.*)_seconds_total$
    as: ""
  metricsQuery: sum(rate(<<.Series>>{<<.LabelMatchers>>}[5m])) by (<<.GroupBy>>)

Events:  <none>
```
