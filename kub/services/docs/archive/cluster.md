# EKS Cluster

## Worker Nodes
    $ kubectl get nodes

## Roles
    $ kubectl get clusterroles
    $ kubectl get clusterrole prometheus-server
    $ kubectl get clusterrole system:basic-user -o yaml
    $ kubectl get clusterrole cluster-admin -o yaml

## RoleBindings
    $ kubectl get rolebindings -n kube-system
    $ kubectl get rolebinding system:controller:bootstrap-signer -n kube-system -o yaml


## Storage

### Ephemeral Storage on Container File System
Your container has a file system and you can store stuff on it if you need to cache thing temporarily. This file system **will go away on container restart**.

We don't have to do anything special to use this file system, just spin up our Pod and use it:

    $ cat containerfs-based-app.yaml
    apiVersion: v1
    kind: Pod
    metadata:
      name: baldur-cfs-pod
    spec:
      containers:
      - name: baldur-ebs-app
        image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
        imagePullPolicy: Always

Our mkt-devops/mkt-tenant-splitter writes data to /install/citng/service/data. This will get stored on the container's file system and will get erased on container restart.

### Amazon EBS CSI Driver

Custom Container Storage Interface (CSI) driver which manages EBS volumes in AWS for Kubernetes.

Note you **most likely won't be using this for your service**. If you have Pods that autoscale this won't work as the EBS volume can only be attached to one Pod at a time and is confined to the availability zone it's created in. This does work well for things like Kafka brokers, PostgreSQL databases, etc that require a hard link between the application and it's disk and want data to be preserved between disk restarts.

#### IAM Stuff

    $ cat example-iam-policy.json
    {
      "Version": "2012-10-17",
      "Statement": [
        {
          "Effect": "Allow",
          "Action": [
            "ec2:AttachVolume",
            "ec2:CreateSnapshot",
            "ec2:CreateTags",
            "ec2:CreateVolume",
            "ec2:DeleteSnapshot",
            "ec2:DeleteTags",
            "ec2:DeleteVolume",
            "ec2:DescribeInstances",
            "ec2:DescribeSnapshots",
            "ec2:DescribeTags",
            "ec2:DescribeVolumes",
            "ec2:DetachVolume"
          ],
          "Resource": "*" #* formatting comment
        }
      ]
    }

Create an IAM policy named Amazon_EBS_CSI_DRIVER

    aws iam create-policy --policy-name Amazon_EBS_CSI_Driver \
    --policy-document file://example-iam-policy.json

The above returns:

    {
        "Policy": {
            "PolicyName": "Amazon_EBS_CSI_Driver",
            "PolicyId": "ANPA53RBKAC275KEZRBF4",
            "Arn": "arn:aws:iam::952478859445:policy/Amazon_EBS_CSI_Driver",
            "Path": "/",
            "DefaultVersionId": "v1",
            "AttachmentCount": 0,
            "PermissionsBoundaryUsageCount": 0,
            "IsAttachable": true,
            "CreateDate": "2019-11-05T19:10:04Z",
            "UpdateDate": "2019-11-05T19:10:04Z"
        }
    }

Get the IAM role name for your stateful worker nodes:

    kubectl -n kube-system describe configmap aws-auth
    Name:         aws-auth
    Namespace:    kube-system
    Labels:       <none>
    Annotations:  <none>

    Data
    ====
    mapRoles:
    ----
    - groups:
      - system:bootstrappers
      - system:nodes
      rolearn: arn:aws:iam::952478859445:role/eksctl-baldur-eks-nodegroup-ng-st-NodeInstanceRole-R1DN8MUUHX3Y
      username: system:node:{{EC2PrivateDNSName}}
    - groups:
      - system:bootstrappers
      - system:nodes
      rolearn: arn:aws:iam::952478859445:role/eksctl-baldur-eks-nodegroup-ng-st-NodeInstanceRole-PKHLRB29JFLU
      username: system:node:{{EC2PrivateDNSName}}

    Events:  <none>

can't tell from the above because the name is truncated, but the second role belongs to the stateful node group (lookup via CF).

Attach the policy all or our node group's roles:

    aws iam attach-role-policy \
    --policy-arn arn:aws:iam::952478859445:policy/Amazon_EBS_CSI_Driver \
    --role-name eksctl-baldur-eks-nodegroup-ng-st-NodeInstanceRole-PKHLRB29JFLU

    aws iam attach-role-policy \
    --policy-arn arn:aws:iam::952478859445:policy/Amazon_EBS_CSI_Driver \
    --role-name eksctl-baldur-eks-nodegroup-ng-st-NodeInstanceRole-R1DN8MUUHX3Y

Deploy:

    $ kubectl apply -k "github.com/kubernetes-sigs/aws-ebs-csi-driver/deploy/kubernetes/overlays/stable/?ref=master"
    serviceaccount/ebs-csi-controller-sa created
    clusterrole.rbac.authorization.k8s.io/ebs-external-attacher-role created
    clusterrole.rbac.authorization.k8s.io/ebs-external-provisioner-role created
    clusterrolebinding.rbac.authorization.k8s.io/ebs-csi-attacher-binding created
    clusterrolebinding.rbac.authorization.k8s.io/ebs-csi-provisioner-binding created
    deployment.apps/ebs-csi-controller created
    daemonset.apps/ebs-csi-node created
    csidriver.storage.k8s.io/ebs.csi.aws.com created

This creates a service account:

    $ kubectl get serviceaccount ebs-csi-controller-sa -n kube-system -o yaml

And a couple of roles and role-bindings that are tied to the service account (for example):

    $ kubectl get clusterrole ebs-external-provisioner-role -o yaml
    $ kubectl get clusterrolebinding ebs-csi-provisioner-binding -o yaml

And deploys the **ebs.csi.aws.com** storage provisioner.

Now we'll use this provisioner to define a StorageClass:

    $ cat ebs-csi-storageclass.yaml
    kind: StorageClass
    apiVersion: storage.k8s.io/v1
    metadata:
      name: ebs-sc
    provisioner: ebs.csi.aws.com
    volumeBindingMode: WaitForFirstConsumer
    reclaimPolicy: Delete

    $ kubectl apply -f ebs-csi-storageclass.yaml
    storageclass.storage.k8s.io/ebs-sc created

    $ kubectl get sc
    NAME            PROVISIONER             AGE
    ebs-sc          ebs.csi.aws.com         17s
    gp2 (default)   kubernetes.io/aws-ebs   26d
    slow            kubernetes.io/aws-ebs   23h

Now lets use the storage class we've created to set up a Pod that uses an EBS volume.

    $ cat ebs-based-app.yaml
    apiVersion: v1
    kind: PersistentVolumeClaim
    metadata:
      name: baldur-ebs-claim
      annotations:
    spec:
      accessModes:
        - ReadWriteOnce
      volumeMode: Filesystem
      resources:
        requests:
          storage: 2Gi
      storageClassName: ebs-sc
    ---
    apiVersion: v1
    kind: Pod
    metadata:
      name: baldur-ebs-pod
    spec:
      initContainers:
      - name: volume-permissions-hack
        image: busybox
        command: ["/bin/chmod","-R","a+rw", "/install/citng/service/data"]
        volumeMounts:
        - name: data-volume
          mountPath: /install/citng/service/data
      containers:
      - name: baldur-ebs-app
        image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
        imagePullPolicy: Always
        volumeMounts:
        - mountPath: /install/citng/service/data
          name: data-volume
      nodeSelector:
        role: stateful
      volumes:
      - name: data-volume
        persistentVolumeClaim:
          claimName: baldur-ebs-claim

        $ kubectl apply -f ebs-based-app.yaml
        persistentvolumeclaim/baldur-ebs-claim created
        pod/baldur-ebs-pod created

The above does the following:

PersistentVolumeClaim
* creates a PersistentVolumeClaim that uses our StorageClass, ebs-sc to make a 2Gi EBS volume
* the volume can only be used by one Pod at a time (ReadWriteOnce)
* the volumeMode: Filesystem sets up a filesytem on the EBS volume, by default ext4

Pod
* initContainers - gives our app permission to write to the disk, there's probably a better way of doing this but I can't figure it out.
* we're mounting the volume at /install/citng/service/data, anything written to this folder will be written to the EBS volume
* nodeSelector: role: stateful means that our Pod will only be deployed to worker nodes with the label:value pair *role: stateful* - our stateful worker nodes

We can set the PersistentVolume and PersistentVolumeClaim created by running:

    $ kubectl get pvc
    NAME               STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
    baldur-ebs-claim   Bound    pvc-06050f15-0012-11ea-bf3f-125b21d5cf2c   2Gi        RWO            ebs-sc         17h
    $ kubectl get pv
    NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                                STORAGECLASS   REASON   AGE
    pvc-06050f15-0012-11ea-bf3f-125b21d5cf2c   2Gi        RWO            Delete           Bound    ci360/baldur-ebs-claim               ebs-sc                  17h

Final note, in our StorageClass we set **reclaimPolicy: Delete** which we can see when we called kubectl get pv. Container restarts **will preserve the data** but if we delete the Pod and recreate it the data will be lost. If we want to keep data even when Pods are deleted we need to set **reclaimPolicy: Retain**.

### Amazon EFS CSI Driver
This is for an autoscaling group of Pods that want to share a permanent file store that's accessible as they scale across availability zones.

Deploy the driver:

    $kubectl apply -k "github.com/kubernetes-sigs/aws-efs-csi-driver/deploy/kubernetes/overlays/stable/?ref=master"
    daemonset.apps/efs-csi-node created
    csidriver.storage.k8s.io/efs.csi.aws.com created

Now we've created a storage driver called **efs.csi.aws.com**. Now we'll have both efs and ebs csi Pods running as a DaemonSet on our cluster:

    $ kubectl get pods -A
    ...
    kube-system            ebs-csi-node-wqsvk                               3/3     Running   0          3h2m
    kube-system            efs-csi-node-292x2                               3/3     Running   0          2m14s
    ...

We can use our new driver to mount the EFS volume on our pods with a couple of caveats:
* The EFS resource needs to be pre-provisioned by an admin - the disk, security group, and mount points on the VPC subnets your k8s cluster runs in
* The EFS disk only has the root (/) directory and is only writable by the root user. The admin will have to mount the disk on a EC2 instance and *sudo chmod 777 /mntpoint* the disk for sassrv to use it. Permissions are by numeric ID so setting up sudo chown sassrv:sassrv /mntpoint won't work.
* more than one application can grab this disk - we'll have to use some convention like writing to **/{island}/{project}/data** to avoid potentially messing up other application's data.
* we also need to think about how we declare the mount point or we may end up writing our data to somewhere we don't expect as I'll show below.

So as will the EBS volume we need to create a PersistentVolumeClaim and a StorageClass, but we also have to create a PersistentVolume pointed at the EFS disk as it's not automatically provisioned:

```sh
$ cat efs-pv.yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: efs-pv
spec:
  capacity:
    storage: 5Gi
  volumeMode: Filesystem
  accessModes:
    - ReadWriteMany
  persistentVolumeReclaimPolicy: Retain
  storageClassName: efs-sc
  csi:
    driver: efs.csi.aws.com
    volumeHandle: fs-9fc07b1e
```

```sh
$ kubectl apply -f efs-pv.yaml
$ kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM                                STORAGECLASS   REASON   AGE
efs-pv                                     5Gi        RWX            Retain           Available                                        efs-sc                  6s
```

Note we need to know the EFS ID **fs-9fc07b1e** for volumeHandle. Again this is something that will need to be pre-provisioned by the k8s admin.

Now we can create a StorageClass that uses the efs.csi.aws.com provisioner:
```sh
$ cat efs-sc.yaml
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: efs-sc
provisioner: efs.csi.aws.com

$ kubectl apply -f storageclass.yaml
storageclass.storage.k8s.io/efs-sc created
$ kubectl get sc
NAME            PROVISIONER             AGE
ebs-sc          ebs.csi.aws.com         41h
efs-sc          efs.csi.aws.com         15s
```

Next create the PersistentVolumeClaim:
```sh
$ cat efs-pvclaim.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: efs-claim
spec:
  accessModes:
    - ReadWriteMany
  storageClassName: efs-sc
  resources:
    requests:
      storage: 5Gi

$ kubectl apply -f efs-pvclaim.yaml
persistentvolumeclaim/efs-claim created
$ kubectl get pvc
NAME               STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
baldur-ebs-claim   Bound    pvc-06050f15-0012-11ea-bf3f-125b21d5cf2c   2Gi        RWO            ebs-sc         41h
efs-claim          Bound    efs-pv
```

And we'll set up a simple application to use the EFS volume (I'm using something that runs as root here because I haven't setup non-root write permissions on the EFS volume yet):

```sh
$ cat pod1.yaml
apiVersion: v1
kind: Pod
metadata:
  name: app2
spec:
  containers:
  - name: app2
    image: busybox
    command: ["/bin/sh"]
    args: ["-c", "while true; do echo $(date -u) >> /data/out2.txt; sleep 5; done"]
    volumeMounts:
    - name: persistent-storage
      mountPath: /data
  volumes:
  - name: persistent-storage
    persistentVolumeClaim:
      claimName: efs-claim

$ kubectl apply -f pod1.yaml
pod/app1 created
$ kubectl get pods
NAME   READY   STATUS    RESTARTS   AGE
app1   1/1     Running   0          5s
```

Let's see if our EFS disk mounted properly on the containers **/data mountpoint**:

```sh
$ kubectl exec -it app1 sh
/ # ls
bin   data  dev   etc   home  proc  root  sys   tmp   usr   var
/ # ls /data
out1.txt  out2.txt
/ # df -h
Filesystem                Size      Used Available Use% Mounted on
overlay                  50.0G      2.4G     47.6G   5% /
tmpfs                    64.0M         0     64.0M   0% /dev
tmpfs                     1.9G         0      1.9G   0% /sys/fs/cgroup
fs-9fc07b1e.efs.us-east-1.amazonaws.com:/
                          8.0E         0      8.0E   0% /data
/dev/nvme0n1p1           50.0G      2.4G     47.6G   5% /dev/termination-log
/dev/nvme0n1p1           50.0G      2.4G     47.6G   5% /etc/resolv.conf
/dev/nvme0n1p1           50.0G      2.4G     47.6G   5% /etc/hostname
/dev/nvme0n1p1           50.0G      2.4G     47.6G   5% /etc/hosts
shm                      64.0M         0     64.0M   0% /dev/shm
tmpfs                     1.9G     12.0K      1.9G   0% /var/run/secrets/kubernetes.io/serviceaccount
tmpfs                     1.9G         0      1.9G   0% /proc/acpi
tmpfs                    64.0M         0     64.0M   0% /proc/kcore
tmpfs                    64.0M         0     64.0M   0% /proc/keys
tmpfs                    64.0M         0     64.0M   0% /proc/latency_stats
tmpfs                    64.0M         0     64.0M   0% /proc/timer_list
tmpfs                    64.0M         0     64.0M   0% /proc/sched_debug
tmpfs                     1.9G         0      1.9G   0% /sys/firmware
```

Above we can see our EFS volume (fs-9fc07b1e.efs.us-east-1.amazonaws.com) mounted at /data with 8GB of elastic storage (8E).

Now I'll set up the EFS volume with the proper non-root write permissions (mounting it to and EC2 instance and running chmod commands) and then use it for our tenant-splitter application:

```sh
$ cat efs-based-app.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-efs-pod
spec:
  containers:
  - name: baldur-efs-app
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    imagePullPolicy: Always
    volumeMounts:
    - mountPath: /data/mkt-devops/mkt-tenant-splitter
      name: data-volume
  nodeSelector:
    role: stateful
  volumes:
  - name: data-volume
    persistentVolumeClaim:
      claimName: efs-claim

$ kubectl apply -f efs-based-app.yaml
pod/baldur-efs-pod created
$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
app1             1/1     Running   0          4h50m
baldur-efs-pod   1/1     Running   0          115s
```

If I log onto my pod i can see the attached efs disk **mounted at /data/mkt-devops/mkt-tenant-splitter**:

```sh
$ kubectl exec -it baldur-efs-pod /bin/bash
bash-4.4$ df -h
Filesystem                Size      Used Available Use% Mounted on
overlay                  50.0G      2.5G     47.4G   5% /
tmpfs                    64.0M         0     64.0M   0% /dev
tmpfs                     1.9G         0      1.9G   0% /sys/fs/cgroup
/dev/nvme0n1p1           50.0G      2.5G     47.4G   5% /dev/termination-log
/dev/nvme0n1p1           50.0G      2.5G     47.4G   5% /etc/resolv.conf
/dev/nvme0n1p1           50.0G      2.5G     47.4G   5% /etc/hostname
/dev/nvme0n1p1           50.0G      2.5G     47.4G   5% /etc/hosts
shm                      64.0M         0     64.0M   0% /dev/shm
fs-9fc07b1e.efs.us-east-1.amazonaws.com:/
                          8.0E         0      8.0E   0% /data/mkt-devops/mkt-tenant-splitter
...
```

Let's check out our quotes and add a few - can use the UI or curl:

```sh
$ curl http://localhost:8888/tenantSplitter/files/quotes
[]
# no quotes yet, lets add some
$ curl http://localhost:8888/tenantSplitter/files/quote?text=baldur
$ curl http://localhost:8888/tenantSplitter/files/quote?text=mymir
$ curl http://localhost:8888/tenantSplitter/files/quote?text=butters
# now we should get three quotes
$ curl http://localhost:8888/tenantSplitter/files/quotes
["","baldur","mymir","butters"]
```

Now let's spin up a ReplicaSet with three of these pods:
```sh
$ kubectl apply -f efs-replicaset.yaml
replicaset.apps/bts-replicaset created
$ kubectl get pods
NAME                   READY   STATUS    RESTARTS   AGE
app1                   1/1     Running   0          5h28m
baldur-efs-pod         1/1     Running   0          30m
bts-replicaset-b4fn9   1/1     Running   0          10s
bts-replicaset-mrd88   1/1     Running   0          10s
bts-replicaset-qk8d5   1/1     Running   0          10s

$ kubectl port-forward bts-replicaset-qk8d5 7000:8080 &
[1] 40802
$ kubForwarding from 127.0.0.1:7000 -> 8080
Forwarding from [::1]:7000 -> 8080

$ kubectl port-forward bts-replicaset-mrd88 7001:8080
Forwarding from 127.0.0.1:7001 -> 8080
Forwarding from [::1]:7001 -> 8080

# we see our old quotes from a new pod
$ curl http://localhost:7000/tenantSplitter/files/quotes
["","baldur","mymir","butters"]
# we can add another quote using the new pod and see it from any other pod
$ curl http://localhost:7000/tenantSplitter/files/quote?text=finnr
$ curl http://localhost:7001/tenantSplitter/files/quotes
["","baldur","mymir","butters","finnr"]
$ curl http://localhost:8888/tenantSplitter/files/quotes
["","baldur","mymir","butters","finnr"]
```

Do we really need to use {island-name}/{project-name} in our mount path? Does it matter how we set up the mount path?
Let's log back into our app1 application that mounted the filesystem as **/data**

```sh
$ kubectl exec -it app1 sh
/ # ls /data
out1.txt    out2.txt    quotes.txt
/ # cat /data/quotes.txt

baldur
mymir
butters
finnr
```
We have our app1 files (out1.txt and out2.txt) being written to the /data folder but we also have quotes.txt even though our baldur-efs-pod and replica-set friends are writting to /data/mkt-devops/mkt-tenant-splitter/quotes.txt.

Let's jump on one of those and see what it looks like:
```sh
$ kubectl exec -it baldur-efs-pod /bin/bash
bash-4.4$ ls /data
mkt-devops
bash-4.4$ ls /data/mkt-devops/mkt-tenant-splitter/
out1.txt    out2.txt    quotes.txt
```
For these Pods everything is showing up at **/data/mkt-devops/mkt-tenant-splitter/** because thats where we mounted the efs disk.
We have:
* app1 mounting efs root /, to /data, so when app1 writes to /data it gets stored at /
* baldur-efs-pod mount efs root /, to /data/mkt-devops/mkt-tenant-splitter, so when baldur-efs-pod writes to /data/mkt-devops/mkt-tenant-splitter it also get stored at /

So the answer is **yes we need to use {island-name}/{project-name}** when we write data, and yes we have to think about our mount paths, or we will most likely clobber each other's stuff!

If we're using a common EFS disk we should:
* standardize on a root mount point like /data
* write to a path that includes our island and project name: /data/{island-name}/{project-name}/

## Autoscaling

### Cluster Autoscaler
Automatically adjusts the number of worker nodes in the cluster.

### Vertical Pod Autoscaler
Automatically adjusts the cpu and memory reservations for your pods to help right size your applications.

### Horizontal Pod Autoscaler (HPA)
Automatically scales the number of Pods in a deployment or replicaset based on the resource's CPU usage.

This requires that a metric source, such as the Kubernetes metrics server, is installed on the cluster. We've already done this as we can see here:
```sh
$ kubectl -n kube-system get deployment/metrics-server
NAME             READY   UP-TO-DATE   AVAILABLE   AGE
metrics-server   1/1     1            1           32d
```
The HPA can scale a ReplicaSet or a Deployment (deployments create replica sets).

We'll create a Deployment that starts with 2 replicas:
```sh
$ cat deployment-baldur.yaml
apiVersion: apps/v1 # for versions before 1.9.0 use apps/v1beta2
kind: Deployment
metadata:
  name: baldur-deployment
spec:
  selector:
    matchLabels:
      app: baldur
  replicas: 2 # tells deployment to run 2 pods matching the template
  template:
    metadata:
      labels:
        app: baldur
    spec:
        containers:
        - name: baldur-ts
          image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
          imagePullPolicy: Always

$ kubectl apply -f deployment-baldur.yaml
$ kubectl get deployments
NAME                READY   UP-TO-DATE   AVAILABLE   AGE
baldur-deployment   2/2     2            2           16s

$ kubectl get rs
NAME                           DESIRED   CURRENT   READY   AGE
baldur-deployment-796664d784   2         2         2       3m57s

$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-796664d784-gnwj2   1/1     Running   0          4m14s
baldur-deployment-796664d784-q5drj   1/1     Running   0          4m14s
```

The HPA Controller queries the resource utilization against the metrics specified in each HorizontalPodAutoscaler definition from either:
* the resource metrics API - for per-pod resource metrics like CPU
* custom metrics API - for all other metrics

Note if a container doesn't have the relevant resource request set, CPU utilization for the pod will not be defined and the autoscaler will not take any action for that metric.

Let's create an HPA for the baldur-deployment:
```sh
$ kubectl autoscale deployment baldur-deployment --min=2 --max=5 --cpu-percent=80
horizontalpodautoscaler.autoscaling/baldur-deployment autoscaled
$ kubectl get hpa
NAME                REFERENCE                      TARGETS         MINPODS   MAXPODS   REPLICAS   AGE
baldur-deployment   Deployment/baldur-deployment   <unknown>/80%   2         5         0          13s

```
Here we can see that the HPA can't see what the Pod requires for CPU. Let's add this to the deployment:
```sh
$ cat deployment-baldur.yaml
apiVersion: apps/v1 # for versions before 1.9.0 use apps/v1beta2
kind: Deployment
metadata:
  name: baldur-deployment
spec:
  selector:
    matchLabels:
      app: baldur
  replicas: 2 # tells deployment to run 2 pods matching the template
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
              cpu: "500m"
              memory: "2048Mi"
```
In the resources section we're requesting 1/2 a CPU (500m) and 2GB of RAM (2048Mi).

We'll recreate our deployment and HPA:
```sh
$ kubectl apply -f deployment-baldur.yaml
$ kubectl autoscale deployment baldur-deployment --min=2 --max=5 --cpu-percent=80
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-deployment   Deployment/baldur-deployment   0%/80%    2         5         2          19s
```
Now we're tracking CPU usage.

```sh
$ kubectl get pods
NAME                                READY   STATUS    RESTARTS   AGE
baldur-deployment-7f4b9bb6d-rz6c9   1/1     Running   0          4m12s
baldur-deployment-7f4b9bb6d-wnf6v   1/1     Running   0          4m12s
$ kubectl port-forward baldur-deployment-7f4b9bb6d-rz6c9 8888:8080

# lets load up the CPU (this will drive up cpu usage to ~95%, for five minutes)
$ curl http://localhost/tenantSplitter/internals/cpuload

$ kubectl get hpa
NAME                REFERENCE                      TARGETS    MINPODS   MAXPODS   REPLICAS   AGE
baldur-deployment   Deployment/baldur-deployment   153%/80%   2         5         2          8m40s
# looks like our CPU usage has spiked, wait a bit and call this again
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-deployment   Deployment/baldur-deployment   77%/80%   2         5         4          10m
# now we have 4 replicas and the average CPU usage is below our target value
$ kubectl get pods
NAME                                READY   STATUS    RESTARTS   AGE
baldur-deployment-7f4b9bb6d-2lss9   1/1     Running   0          107s
baldur-deployment-7f4b9bb6d-754v4   1/1     Running   0          10m
baldur-deployment-7f4b9bb6d-gf9db   1/1     Running   0          10m
baldur-deployment-7f4b9bb6d-n9r7b   1/1     Running   0          107s

# let's drop the load off now
$ curl http://localhost:8888/tenantSplitter/internals/cpuunload

# wait a few minutes and check out the deployment
$ kubectl get deployments
NAME                READY   UP-TO-DATE   AVAILABLE   AGE
baldur-deployment   2/2     2            2           25m
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-deployment   Deployment/baldur-deployment   0%/80%    2         5         2          20m
$ kubectl get pods
NAME                                READY   STATUS    RESTARTS   AGE
baldur-deployment-7f4b9bb6d-754v4   1/1     Running   0          21m
baldur-deployment-7f4b9bb6d-gf9db   1/1     Running   0          21m
```


## Authentication

EKS uses IAM for authentication and RBAC for authorization.

We've already installed the aws IAM authenticator for kubernetes. We can see who we're calling as with:

```sh
$ aws sts get-caller-identity
{
    "UserId": "AROAIBMAVFQB4JGH4XO34:Randall.Zingle@sas.com",
    "Account": "952478859445",
    "Arn": "arn:aws:sts::952478859445:assumed-role/admin/Randall.Zingle@sas.com"
}
```
$ kubectl get configmaps -n kube-system
NAME                                 DATA   AGE
aws-auth                             1      33d
coredns                              1      33d
eks-certificates-controller          0      32d
extension-apiserver-authentication   6      33d
kube-proxy                           1      33d
kube-proxy-config                    1      33d

$ kubectl describe configmap aws-auth -n kube-system
Name:         aws-auth
Namespace:    kube-system
Labels:       <none>
Annotations:  <none>

Data
====
mapRoles:
----
- groups:
  - system:bootstrappers
  - system:nodes
  rolearn: arn:aws:iam::952478859445:role/eksctl-baldur-eks-nodegroup-ng-st-NodeInstanceRole-R1DN8MUUHX3Y
  username: system:node:{{EC2PrivateDNSName}}
- groups:
  - system:bootstrappers
  - system:nodes
  rolearn: arn:aws:iam::952478859445:role/eksctl-baldur-eks-nodegroup-ng-st-NodeInstanceRole-PKHLRB29JFLU
  username: system:node:{{EC2PrivateDNSName}}

Events:  <none>

$ kubectl get configmap aws-auth -n kube-system -o yaml
apiVersion: v1
data:
  mapRoles: |
    - groups:
      - system:bootstrappers
      - system:nodes
      rolearn: arn:aws:iam::952478859445:role/eksctl-baldur-eks-nodegroup-ng-st-NodeInstanceRole-R1DN8MUUHX3Y
      username: system:node:{{EC2PrivateDNSName}}
    - groups:
      - system:bootstrappers
      - system:nodes
      rolearn: arn:aws:iam::952478859445:role/eksctl-baldur-eks-nodegroup-ng-st-NodeInstanceRole-PKHLRB29JFLU
      username: system:node:{{EC2PrivateDNSName}}
kind: ConfigMap
metadata:
  creationTimestamp: "2019-10-10T12:52:36Z"
  name: aws-auth
  namespace: kube-system
  resourceVersion: "858"
  selfLink: /api/v1/namespaces/kube-system/configmaps/aws-auth
  uid: d5d0f7c2-eb5c-11e9-86b2-0acc0a4efb33


If your cluster supports IAM roles for service accounts, it will have an OpenID Connect issuer URL associated with it:
$ aws eks describe-cluster --name baldur-eks --query "cluster.identity.oidc.issuer" --output text
https://oidc.eks.us-east-1.amazonaws.com/id/DF08F33D88552F7657A214DDEFBCB8EE

To use IAM roles for service accounts in your cluster, you must create an OIDC identity provider:
$ eksctl utils associate-iam-oidc-provider --name baldur-eks --approve
[ℹ]  using region us-east-1
[ℹ]  will create IAM Open ID Connect provider for cluster "baldur-eks" in "us-east-1"
[✔]  created IAM Open ID Connect provider for cluster "baldur-eks" in "us-east-1"

After you have enabled the IAM OIDC identity provider for your cluster, you can create IAM roles to associate with a service account in your cluster:

Create an IAM Role with attached Policy that allows S3 access (BaldurRole):
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "s3:*",
            "Resource": "*"
        }
    ]
}
```

We have to set up our cluster's OIDC provider as the principal that will assume the role and we'll further constrain it to a specific service account "ci360:finnr-sa":

```json
{
  "Type": "AWS::IAM::Role",
  "Properties": {
    "AssumeRolePolicyDocument": {
      "Statement": [
        {
          "Action": [
            "sts:AssumeRoleWithWebIdentity"
          ],
          "Condition": {
            "StringEquals": {
              "oidc.eks.us-east-1.amazonaws.com/id/DF08F33D88552F7657A214DDEFBCB8EE:aud": "sts.amazonaws.com",
              "oidc.eks.us-east-1.amazonaws.com/id/DF08F33D88552F7657A214DDEFBCB8EE:sub": "system:serviceaccount:ci360:finnr-sa"
            }
          },
          "Effect": "Allow",
          "Principal": {
            "Federated": "arn:aws:iam::952478859445:oidc-provider/oidc.eks.us-east-1.amazonaws.com/id/DF08F33D88552F7657A214DDEFBCB8EE"
          }
        }
      ],
      "Version": "2012-10-17"
    },
    "ManagedPolicyArns": [
      "arn:aws:iam::aws:policy/AmazonS3FullAccess"
    ]
  }
}
```

Create the finnr-sa ServiceAccount:
```sh
$ cat finnr-service-account.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: finnr-sa
  namespace: ci360
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::952478859445:role/eksctl-baldur-eks-addon-iamserviceaccount-ci-Role1-1PHEIJTBLHM67

$ kubectl apply -f finnr-service-account.yaml
serviceaccount/baldur-service-account created
$ kubectl get sa
NAME       SECRETS   AGE
default    1         18d
finnr-sa   1         12s
```

Note you can also use eksctl to create both the role and the service account:
```sh
eksctl create iamserviceaccount --name finnr-sa --namespace ci360 \
--cluster baldur-eks --attach-policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess --approve  --override-existing-serviceaccounts
```

For older subversions of kubernetes 1.14 we also have to add a securityContext section to our deployment or our container won't be able to read the service account token that it's provided and will fail with the error: Permission denied: '/var/run/secrets/eks.amazonaws.com/serviceaccount/token'.

Our deployment now looks like this:
```yaml
apiVersion: apps/v1 # for versions before 1.9.0 use apps/v1beta2
kind: Deployment
metadata:
  name: baldur-deployment
spec:
  selector:
    matchLabels:
      app: baldur
  replicas: 2 # tells deployment to run 2 pods matching the template
  template:
    metadata:
      labels:
        app: baldur
    spec:
      serviceAccountName: finnr-sa
      securityContext:
        fsGroup: 1000
      containers:
      - name: baldur-ts
        image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
        imagePullPolicy: Always
        resources:
          requests:
            cpu: "500m"
            memory: "2048Mi"
```
Above we're hardwiring the sas linux group number (1000) into the security context.

If we deploy the app, port forward it, and hit the "Get S3 Bucket List", we'll successfully connect to S3 and get the list of bucket names.


## Helm

### Install Helm
install helm version 3

add a chart repository:
$ helm repo add stable https://kubernetes-charts.storage.googleapis.com/

search the repo that we've named stable:
$ helm search repo stable

$ helm repo update
Hang tight while we grab the latest from your chart repositories...
...Successfully got an update from the "stable" chart repository
Update Complete. ⎈ Happy Helming!⎈

### Install a Sample Chart
$ helm install stable/mysql --generate-name
NAME: mysql-1573854453
LAST DEPLOYED: Fri Nov 15 16:47:39 2019
NAMESPACE: ci360
STATUS: deployed
REVISION: 1
NOTES:
MySQL can be accessed via port 3306 on the following DNS name from within your cluster:
mysql-1573854453.ci360.svc.cluster.local

To get your root password run:

    MYSQL_ROOT_PASSWORD=$(kubectl get secret --namespace ci360 mysql-1573854453 -o jsonpath="{.data.mysql-root-password}" | base64 --decode; echo)

To connect to your database:

1. Run an Ubuntu pod that you can use as a client:

    kubectl run -i --tty ubuntu --image=ubuntu:16.04 --restart=Never -- bash -il

2. Install the mysql client:

    $ apt-get update && apt-get install mysql-client -y

3. Connect using the mysql cli, then provide your password:
    $ mysql -h mysql-1573854453 -p

To connect to your database directly from outside the K8s cluster:
    MYSQL_HOST=127.0.0.1
    MYSQL_PORT=3306

    # Execute the following command to route the connection:
    kubectl port-forward svc/mysql-1573854453 3306

    mysql -h ${MYSQL_HOST} -P${MYSQL_PORT} -u root -p${MYSQL_ROOT_PASSWORD}

Now let's see what we've installed with helm:

```sh
$ helm ls
NAME            	NAMESPACE	REVISION	UPDATED                             	STATUS  	CHART      	APP VERSION
mysql-1573854453	ci360    	1       	2019-11-15 16:47:39.118032 -0500 EST	deployed	mysql-1.4.0	5.7.27
```

### Unistall a Chart

Now let's unistall the mysql stuff
```sh
$ helm uninstall mysql-1573854453
release "mysql-1573854453" uninstalled
$ helm ls
NAME	NAMESPACE	REVISION	UPDATED	STATUS	CHART	APP VERSION
```
