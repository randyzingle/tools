# Kubernetes Commands

## General Cluster Info

```sh
# What cluster am I hitting?
$ kubectl config # get list of config commands
$ kubectl config get-clusters # get all clusters in my config file
minikube
arn:aws:eks:us-east-1:952478859445:cluster/baldur-eks
baldur-eks.us-east-1.eksctl.io
$ kubectl config use-context arn:aws:eks:us-east-1:952478859445:cluster/baldur-eks
$ kubectl config current-context
arn:aws:eks:us-east-1:952478859445:cluster/baldur-eks

# General Cluster Information
$ kubectl cluster-info
Kubernetes master is running at https://DF08F33D88552F7657A214DDEFBCB8EE.gr7.us-east-1.eks.amazonaws.com
CoreDNS is running at https://DF08F33D88552F7657A214DDEFBCB8EE.gr7.us-east-1.eks.amazonaws.com/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
Metrics-server is running at https://DF08F33D88552F7657A214DDEFBCB8EE.gr7.us-east-1.eks.amazonaws.com/api/v1/namespaces/kube-system/services/https:metrics-server:/proxy
$ kubectl cluster-info dump
... everything! ...

# Servers that your apps run on
$ kubectl get nodes
NAME                            STATUS   ROLES    AGE     VERSION
ip-10-240-11-33.ec2.internal    Ready    <none>   2m55s   v1.14.6-eks-5047ed
ip-10-240-11-49.ec2.internal    Ready    <none>   2m57s   v1.14.6-eks-5047ed
ip-10-240-12-151.ec2.internal   Ready    <none>   3m11s   v1.14.6-eks-5047ed

# Default service that runs in every cluster
$ kubectl get services -n default
NAME             TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE
kubernetes       ClusterIP   172.20.0.1      <none>        443/TCP   57d

# Default roles and role bindings
$ kubectl get roles -n kube-system
$ kubectl get clusterroles -n kube-system
$ kubectl get rolebindings -n kube-system

# Set your kubectl context to use the default namespace
$ kubectl config set-context --current --namespace=default

# Set your kubectl context to use the ci360 namespace
$ kubectl config set-context --current --namespace=ci360

```

## Pods
A Pod is a collection of containers and volumes running in the same execution environment (share post name / port space). Pods are the smallest deployable artifact in a Kubernetes cluster.

### Pod Commands
Create a Pod:
```sh
$ cat simple-pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
$ kubectl apply -f simple-pod.yaml
pod/baldur-tensplit created

$ kubectl get pod baldur-tensplit -o wide
NAME              READY   STATUS    RESTARTS   AGE   IP             NODE                           NOMINATED NODE   READINESS GATES
baldur-tensplit   1/1     Running   0          67s   10.240.28.51   ip-10-240-24-33.ec2.internal   <none>           <none>
```
Get logs for the Pod:
```sh
$ kubectl logs baldur-tensplit
VM settings:
    Max. Heap Size (Estimated): 1.85G
    Ergonomics Machine Class: server
    Using VM: OpenJDK 64-Bit Server VM
...
```
Log onto the container:
```sh
$ kubectl exec -it baldur-tensplit /bin/bash
bash-4.4$ ps -ef | grep java
    1 sassrv    0:40 java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -XshowSettings:vm -Dlogging.config=classpath:logback-spring.xml -jar /install/citng/service/service.jar
   49 sassrv    0:00 grep java
bash-4.4$ exit
exit
```
Add a label to a Pod:
```sh
$ kubectl label pod baldur-tensplit color=green
pod/baldur-tensplit

$ kubectl describe pod baldur-tensplit
Name:         baldur-tensplit
Namespace:    ci360
Priority:     0
Node:         ip-10-240-24-33.ec2.internal/10.240.24.33
Start Time:   Mon, 09 Dec 2019 12:51:26 -0500
Labels:       color=green
...
```
Clean things up:
```sh
$ kubectl delete -f simple-pod.yaml
pod "baldur-tensplit" deleted
```
We can add information to the Pod manifest about the container's exposed ports. This is purely informational and doesn't effect the operation of the Pod in any way. If we change the Pod manifest to this:
```sh
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    ports:
      - containerPort: 8080
        name: http
        protocol: TCP
```
Then describing the pod will change from this:
```sh
Containers:
  baldurpod:
    Container ID:
    Image:          952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    Image ID:
    Port:           <none>
    Host Port:      <none>
    State:          Waiting
      Reason:       ContainerCreating
    Ready:          False
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-kv5zx (ro)
```
To this:
```sh
Containers:
  baldurpod:
    Container ID:   docker://5832ad2db01d9cf35e3bf1fe420d6bb723fab5f94a2e2ddad16dc7e892b503a9
    Image:          952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    Image ID:       docker-pullable://952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter@sha256:a4b6a761ba8dd7d44933046b683a790c58d998a3294a1c6dafdaf7716c29472f
    Port:           8080/TCP
    Host Port:      0/TCP
    State:          Running
      Started:      Mon, 09 Dec 2019 13:30:26 -0500
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-kv5zx (ro)
```
### How do we access the Pod?
For development work we can use simple port forwarding. Start our application up again and then run:
```sh
# will forward port 8000 on our laptop to port 8080 on the container
$ kubectl port-forward baldur-tensplit 8000:8080
$ curl http://localhost:8000/tenantSplitter/commons/ping
OK
```
With the port-forward command listed above we're creating a tunnel from our box, through the Kubernetes master, to an instance of the Pod running on one fo the worker nodes.

Of course, that's not how well expose our application in production or even a shared development environment. For that we'll use a Kubernetes Service Object as I'll explain later in this document.

### Liveness Probes
Let's add a liveness probe to our application. Kubernetes will ping this endpoint and will restart the Pod if the ping fails. That means we **should NOT check our external dependencies in the liveness probe**. We don't want to restart if an external dependency is down. We talk about this in the next section (Readiness Probes).

```sh
$ cat simple-pod-liveness.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    livenessProbe:
      httpGet:
        path: /tenantSplitter/commons/ping-wtf
        port: 8080
      initialDelaySeconds: 10
      timeoutSeconds: 1
      periodSeconds: 10
      failureThreshold: 3
$ kubectl apply -f simple-pod-liveness.yaml
pod/baldur-tensplit created
```
Above we are asking Kubernetes to ping the endpoint, */tenantSplitter/commons/ping-wtf* at port *8080*, after an initial delay of 10 secs then every 10 seconds after that. It waits 1 sec for a response and if it fails 3 times in a row then Kubernetes will restart the Pod. Since this Pod doesn't have a *ping-wtf* endpoint we should see it restarting every 30 secs:
```sh
$ kubectl get pod baldur-tensplit
NAME              READY   STATUS    RESTARTS   AGE
baldur-tensplit   1/1     Running   0          13s
$ kubectl get pod baldur-tensplit
NAME              READY   STATUS    RESTARTS   AGE
baldur-tensplit   1/1     Running   1          41s
$ kubectl get pod baldur-tensplit
NAME              READY   STATUS    RESTARTS   AGE
baldur-tensplit   1/1     Running   1          61s
$ kubectl get pod baldur-tensplit
NAME              READY   STATUS    RESTARTS   AGE
baldur-tensplit   1/1     Running   2          80s

# Let's kill it
$ kubectl delete -f simple-pod-liveness.yaml
pod "baldur-tensplit" deleted
```
If we edit the pod manifest and change the endpoint to */tenantSplitter/commons/ping*, which returns a simple OK, then we should be good:
```sh
$ cat simple-pod-liveness.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    livenessProbe:
      httpGet:
        path: /tenantSplitter/commons/ping
        port: 8080
      initialDelaySeconds: 10
      timeoutSeconds: 1
      periodSeconds: 10
      failureThreshold: 3
$ kubectl apply -f simple-pod-liveness.yaml
pod/baldur-tensplit created
$ kubectl get pod baldur-tensplit
NAME              READY   STATUS    RESTARTS   AGE
baldur-tensplit   1/1     Running   0          118s
```
### Readiness Probes
Readiness describes when a container is ready to process data, serve user requests, and in general be useful. Containers that fail readiness checks are removed from service load balancers and your internal code should hold off on any of it's work, like reading data from Kafka or message queues. The manifest entry is identical to the liveness probe but the endpoint **SHOULD be verifying that any required external dependencies are available**.

```sh
$ cat simple-pod-live-ready.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    livenessProbe:
      httpGet:
        path: /tenantSplitter/commons/ping
        port: 8080
      initialDelaySeconds: 10
      timeoutSeconds: 1
      periodSeconds: 10
      failureThreshold: 3
    readinessProbe:
      httpGet:
        path: /tenantSplitter/commons/healthcheck
        port: 8080
      initialDelaySeconds: 10
      timeoutSeconds: 1
      periodSeconds: 10
      failureThreshold: 3
$ kubectl apply -f simple-pod-live-ready.yaml
pod/baldur-tensplit created
$ kubectl get pod baldur-tensplit
NAME              READY   STATUS    RESTARTS   AGE
baldur-tensplit   0/1     Running   0          3m
```

### Resource Management
We are moving to Docker / Kubernetes to save money. To do this we need to pack as many Pods as possible onto each physical server. In the following we're going to specify CPU and memory limits. You can find more details on which resources you can specify and the terminology used to define the limits in the [Kubernetes Documentation](https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/).

The resource scheduler will pack Pods onto nodes based on the **resource requests** and will not allow this to exceed the capacity of the machine. Kubernetes will **pass the CPU and memory limits to the container runtime**. Our java startup command flag **"-XX:+UseCGroupMemoryLimitForHeap"** will use the memory **limits** to set the max heap size.

In the following we'll request a minimum of 64 MB of RAM and 1/4 of a CPU with a maximum of 128 MB of RAM and 1/2 of a CPU. In AWS CPU == vCPU.

```sh
$ cat simple-pod-resources.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    resources:
      requests:
        memory: "64Mi"
        cpu: "250m"
      limits:
        memory: "128Mi"
        cpu: "500m"

$ kubectl apply -f simple-pod-resources.yaml
pod/baldur-tensplit created
$ kubectl describe pod baldur-tensplit
Name:         baldur-tensplit
Namespace:    ci360
...
Containers:
  baldurpod:
    Container ID:   docker://ff42f1c8c6f7eaccaf7c654bf24bc0b5b96aa82278a80d833713edbff7ce1019
    Image:          952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    Image ID:       docker-pullable://952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter@sha256:a4b6a761ba8dd7d44933046b683a790c58d998a3294a1c6dafdaf7716c29472f
    Port:           <none>
    Host Port:      <none>
    State:          Running
      Started:      Mon, 09 Dec 2019 14:40:05 -0500
    Ready:          True
    Restart Count:  0
    Limits:
      cpu:     500m
      memory:  128Mi
    Requests:
      cpu:        250m
      memory:     64Mi
...
# How are we doing?
$ kubectl get pods baldur-tensplit
NAME              READY   STATUS             RESTARTS   AGE
baldur-tensplit   0/1     CrashLoopBackOff   6          14m
```
It's crashing and restarting, looks like something is off, let's check the Pod:
```sh
$ kubectl describe pod baldur-tensplit
...
Containers:
  baldurpod:
    Container ID:   docker://f7dc09c673c24c2076333bea7e9b96ca3eecd6173e6d24c58ef8962029eb1130
    Image:          952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    Image ID:       docker-pullable://952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter@sha256:a4b6a761ba8dd7d44933046b683a790c58d998a3294a1c6dafdaf7716c29472f
    Port:           <none>
    Host Port:      <none>
    State:          Waiting
      Reason:       CrashLoopBackOff
    Last State:     Terminated
      Reason:       OOMKilled
      Exit Code:    137
      Started:      Mon, 09 Dec 2019 14:56:04 -0500
      Finished:     Mon, 09 Dec 2019 14:56:51 -0500
...
```

OOMKilled - we need more than the limit we've set to run the application. When we run baldur-tensplit locally we can see that it sits at around 340MB of heap usage even when no one is using it. Let's boost the limit to 512MB of RAM and restart the Pod:

```sh
$ cat simple-pod-resources.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
    resources:
      requests:
        memory: "64Mi"
        cpu: "250m"
      limits:
        memory: "512Mi"
        cpu: "500m"
$ kubectl apply -f simple-pod-resources.yaml
pod/baldur-tensplit created

# And we're happy now
$ kubectl get pod baldur-tensplit
NAME              READY   STATUS    RESTARTS   AGE
baldur-tensplit   1/1     Running   0          39s

# We can check the logs to see what max heap size we used - should be just under the ~500MB we requested:
$ kubectl logs baldur-tensplit
VM settings:
    Max. Heap Size (Estimated): 494.94M
    Ergonomics Machine Class: server
    Using VM: OpenJDK 64-Bit Server VM
```

#### CPU Limits
Note, if a Pod with a resource **request of 0.5 CPUs** is the only Pod on a box with 2 CPUs, **and you haven't set any limits**, it will use all of both CPUs if needed. If a second such Pod is dropped on the box then they will both be scaled back to 1 CPU each and if 2 more are added they will be scaled back to 0.5 CPUs each. No further Pods will be scheduled to this node since it will now be considered full based on your resource requests. If you set the **limit to 0.5 CPUs** as well then you will only get 1/2 of the CPU even if it is otherwise idle.

#### Memory Limits
With the way the Java Heap is setup the JVM will restrict you to you're memory limit and will die with an OOM error if you exceed it. For non-Java apps you can blow through your memory limit but Kubernetes will kill your Pod (eventually) if you do.

## Controllers
### ReplicaSet
From the docs - a ReplicaSet's purpose is to maintain a stable set of replica Pods running at any given time. As such, it is often used to guarantee the availability of a specified number of identical Pods. ReplicaSets are designed for stateless (or nearly stateless) services.

A ReplicaSet is defined with fields including a **selector** that specifies how to identify Pods it can acquire, a **number** of replicas indicating how many Pods it should be maintaining, and a **pod template** specifying the data of new Pods it should create. The ReplicaSet then creates and maintains the desired number of Pods.

A **Deployment** is a higher-level concept that manages ReplicaSets. There's little reason to create a ReplicaSet without a Deployment (which manages updates / versioning) so we'll look at both together in the following section.

### Deployments
The following file *deployment-simple.yaml* will create a **Deployment** which will create a **ReplicaSet** that in turn will create two **Pods** using the mkt-devops/mkt-tenant-splitter container image:

```sh
$ cat deployment-simple.yaml
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
$ kubectl apply -f deployment-simple.yaml
```
When can examine the resources created as follows:
```sh
# Our deployment
$ kubectl get deployments
NAME                READY   UP-TO-DATE   AVAILABLE   AGE
baldur-deployment   2/2     2            2           3m52s

# The deployment has created a replica set and added the label app=baldur
$ kubectl get replicasets --show-labels
NAME                           DESIRED   CURRENT   READY   AGE     LABELS
baldur-deployment-785975f6f4   2         2         2       4m20s   app=baldur,pod-template-hash=785975f6f4

# The replicaset has created two pods and has added the label app=baldur
$ kubectl get pods --show-labels
NAME                                 READY   STATUS    RESTARTS   AGE     LABELS
baldur-deployment-785975f6f4-lchg5   1/1     Running   0          4m40s   app=baldur,pod-template-hash=785975f6f4
baldur-deployment-785975f6f4-pb94s   1/1     Running   0          4m40s   app=baldur,pod-template-hash=785975f6f4
```
Note the ReplicaSet manages Pods based entirely on the label selector information, in this case app=baldur. If there are existing Pods with this label-value pair then they will be adopted by the ReplicaSet.

If we want to see all the Pods that the ReplicaSet will control when can use a label selector:
```sh
$ kubectl get pods -l app=baldur
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-785975f6f4-lchg5   1/1     Running   0          11m
baldur-deployment-785975f6f4-pb94s   1/1     Running   0          11m
```
We can query a Pod directly to see if it's being managed by a ReplicaSet. As we can see below the Pod baldur-deployment-785975f6f4-pb94s is being managed by the ReplicaSet baldur-deployment-785975f6f4.
```sh
$ kubectl get pod baldur-deployment-785975f6f4-pb94s -o yaml
apiVersion: v1
kind: Pod
metadata:
  annotations:
    kubernetes.io/psp: eks.privileged
  creationTimestamp: "2019-12-10T19:26:45Z"
  generateName: baldur-deployment-785975f6f4-
  labels:
    app: baldur
    pod-template-hash: 785975f6f4
  name: baldur-deployment-785975f6f4-pb94s
  namespace: ci360
  ownerReferences:
  - apiVersion: apps/v1
    blockOwnerDeletion: true
    controller: true
    kind: ReplicaSet
    name: baldur-deployment-785975f6f4
    uid: 00ae55a3-1b83-11ea-8332-12900007cf77
    ...
```
We can scale out our deployment manually by running
```sh
$ kubectl scale deployment baldur-deployment --replicas=4
deployment.extensions/baldur-deployment scaled

# Let's check it out
$ kubectl get deployments
NAME                READY   UP-TO-DATE   AVAILABLE   AGE
baldur-deployment   4/4     4            4           46m
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-785975f6f4-djqv8   1/1     Running   0          13s
baldur-deployment-785975f6f4-j7b9n   1/1     Running   0          13s
baldur-deployment-785975f6f4-lchg5   1/1     Running   0          46m
baldur-deployment-785975f6f4-pb94s   1/1     Running   0          46m
```
But, of course we wouldn't want to have to do such a thing manually. In a latter section we'll look at how we can use the Horizontal Pod Autoscaler (HPA) to monitor metrics and then scale the Pods in/out based on metric values.

#### Rolling out new versions
Deployments exist to manage the release of new versions of your software. They allow you to easily move from one version of your code to the next. Pods and ReplicaSets are tied to specific container images. When you upgrade Deployments allow you to rollout the new version in a configurable manner. It allows you to wait a configurable amount of time between upgrading Pods, using health checks (the **readiness check**) to ensure that the new version of the application is operating correctly. It stops the deployment if too many failures occur.

Our current Deployment has created a ReplicaSet using the **baldur** version of our mkt-tenant-splitter application:
```sh
$ kubectl get replicasets -o wide
NAME                           DESIRED   CURRENT   READY   AGE   CONTAINERS   IMAGES                                                                               SELECTOR
baldur-deployment-785975f6f4   4         4         4       54m   baldur-ts    952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur   app=baldur,pod-template-hash=785975f6f4
```

Let's rollout the **mymir** version our our application. The modified yaml file is below, specifying the mymir version of our app. We've also added a rollout strategy that specifies that we'll be doing a rolling update with one Pod being available throughout the rollout.

```sh
$ cat deployment-rollout.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: baldur-deployment
spec:
  selector:
    matchLabels:
      app: baldur
  replicas: 4
  strategy:
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 1
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: baldur
    spec:
        containers:
        - name: baldur-ts
          image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
$ kubectl apply -f deployment-rollout.yaml
deployment.apps/baldur-deployment configured
```
Let's check it out:
```sh
$ kubectl get deployments
NAME                READY   UP-TO-DATE   AVAILABLE   AGE
baldur-deployment   4/4     4            4           70m
$ kubectl get rs -o wide
NAME                           DESIRED   CURRENT   READY   AGE     CONTAINERS   IMAGES                                                                               SELECTOR
baldur-deployment-785975f6f4   0         0         0       70m     baldur-ts    952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur   app=baldur,pod-template-hash=785975f6f4
baldur-deployment-86b9dbbc86   4         4         4       3m21s   baldur-ts    952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir    app=baldur,pod-template-hash=86b9dbbc86
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-86b9dbbc86-592nh   1/1     Running   0          3m40s
baldur-deployment-86b9dbbc86-df5f5   1/1     Running   0          3m36s
baldur-deployment-86b9dbbc86-l2xck   1/1     Running   0          3m40s
baldur-deployment-86b9dbbc86-qngzb   1/1     Running   0          3m36s
```
We can see above that we still have our old ReplicaSet with the *baldur* version of our application, but with zero Pods running. This is kept around so that we can rollback to this version if we need to. The new ReplicaSet is running our four Pods with the new *mymir* version of our application.

We can examine the full history of our Deployment's rollouts (we could have included a change-cause field in the yaml file and that would show up in the column below):
```sh
$ kubectl rollout history deployment baldur-deployment
deployment.extensions/baldur-deployment
REVISION  CHANGE-CAUSE
1         <none>
2         <none>
```

If we decided we needed to rollback to the previous version we could with:
```sh
$ kubectl rollout undo deployment baldur-deployment

# Let's check it out
$ kubectl get rs -o wide
NAME                           DESIRED   CURRENT   READY   AGE   CONTAINERS   IMAGES                                                                               SELECTOR
baldur-deployment-785975f6f4   4         4         4       78m   baldur-ts    952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur   app=baldur,pod-template-hash=785975f6f4
baldur-deployment-86b9dbbc86   0         0         0       11m   baldur-ts    952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir    app=baldur,pod-template-hash=86b9dbbc86
# Good, we've rolled back to the baldur version of our application

# And we can see below that we're now on version 3 of our deployment history, and version 1 is gone (because it's now the latest version = 3)
$ kubectl rollout history deployment baldur-deployment
deployment.extensions/baldur-deployment
REVISION  CHANGE-CAUSE
2         <none>
3         <none>
```

So far we've been using the **RollingUpdate** strategy to deploy our new version. We could have also used the **Recreate** strategy which deletes all Pods and then spins up new ones, with the new container image version. This, of course, means we have an outage. If that's fine, then this is a simpler strategy to program around.

#### Are we ready?
Are the Pods we've created really healthy? We know something is running or Kubernetes would have bounced the Pod but we need to add our liveness and readiness checks back in:

```sh
$ cat deployment-robust.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: baldur-deployment
spec:
  selector:
    matchLabels:
      app: baldur
  replicas: 4
  strategy:
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 1
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: baldur
    spec:
        containers:
        - name: baldur-ts
          image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
          livenessProbe:
            httpGet:
              path: /tenantSplitter/commons/ping
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 1
            periodSeconds: 10
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /tenantSplitter/commons/healthcheck
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 1
            periodSeconds: 10
            failureThreshold: 3

$ kubectl apply -f deployment-robust.yaml

(base) razing@mlb727 services $ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-785975f6f4-2mwxt   1/1     Running   0          17h
baldur-deployment-785975f6f4-6hphg   1/1     Running   0          17h
baldur-deployment-785975f6f4-mbrmn   1/1     Running   0          17h
baldur-deployment-847b45d946-2fnbd   0/1     Running   0          26s
baldur-deployment-847b45d946-tcp7k   0/1     Running   0          26s

# Wait a bit ...
(base) razing@mlb727 services $ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-785975f6f4-2mwxt   1/1     Running   0          17h
baldur-deployment-785975f6f4-6hphg   1/1     Running   0          17h
baldur-deployment-785975f6f4-mbrmn   1/1     Running   0          17h
baldur-deployment-847b45d946-2fnbd   0/1     Running   0          6m16s
baldur-deployment-847b45d946-tcp7k   0/1     Running   0          6m16s
```

Our readinessProbe is failing so the deployment isn't killing the old Pods yet (I've hard-wired an HTTP 500 for the commons/healthcheck). Let's fix and redeploy our docker image to ECR, rollback the deployment are re-deploy:

#### Autoscaling a ReplicaSet

We typically want to start with what we think is the minimum number of Pods we need to handle traffic and then scale out if traffic increases (and back in if traffic drops). ReplicaSets can work with the Horizontal Pod Autoscaler (HPA), and the metrics server to achieve this.

The HPA Controller queries the resource utilization against the metrics specified in each HPA definition from either:
* the resource metrics API - for per-pod resource metrics like CPU
* custom metrics API - for all other metrics

Note if a container doesn't have the relevant resource request set (in our case below, the CPU utilization), then CPU utilization for the pod will not be defined and the autoscaler will not take any action for that metric.

We'll update our deployment to include a resources section. We will also comment out the *replicas: 2* as will have the HPA determine the number of required replicas:
```sh
# Add resources section
$ cat deployment-robust.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: baldur-deployment
spec:
  selector:
    matchLabels:
      app: baldur
  #replicas: 2
  strategy:
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 1
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: baldur
    spec:
      containers:
      - name: baldur-ts
        image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2048Mi"
            cpu: "1000m"
        imagePullPolicy: Always
        livenessProbe:
          httpGet:
            path: /tenantSplitter/commons/ping
            port: 8080
          initialDelaySeconds: 60
          timeoutSeconds: 1
          periodSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /tenantSplitter/commons/healthcheck
            port: 8080
          initialDelaySeconds: 60
          timeoutSeconds: 1
          periodSeconds: 10
          failureThreshold: 3

# Deploy it
$ kubectl apply -f deployment-robust.yaml
deployment.apps/baldur-deployment configured
```
In the resources section we're requesting 1/2 a CPU (500m) and 1/2 GB of RAM (512Mi).

Now we'll attach an HPA to the baldur-deployment:
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
$ kubectl get hpa
NAME                REFERENCE                      TARGETS         MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   <unknown>/50%   2         10        0          5s
# wait a few seconds until it grabs the pods from our deployment ...
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   0%/50%    2         10        2          45s
```
Above we see that we've created an HPA that will limit the baldur-deployment to a minimum of 2 Pods, maximum of 10, and will scale out if we hit 50% CPU usage (the <unknown> and 0% under TARGETS is the measured value for CPU usage in the metric server).

With nothing happening in our application this shouldn't have changed the number of Pods that we have running:
```sh
$ kubectl get pods -o wide
NAME                                 READY   STATUS    RESTARTS   AGE   IP              NODE                            NOMINATED NODE   READINESS GATES
baldur-deployment-7c5458465c-2j5qg   1/1     Running   0          12m   10.240.11.54    ip-10-240-15-40.ec2.internal    <none>           <none>
baldur-deployment-7c5458465c-955mj   1/1     Running   0          12m   10.240.26.146   ip-10-240-26-203.ec2.internal   <none>           <none>
```

Let's ramp up the CPU usage one of the Pods, checking to see if the HPA scales them out. We'll hit the Pods directly (I've opened up the Kubernetes worker-node security groups so that we can hit the Pods from the SAS LAN).
```sh
$ curl http://10.240.11.54:8080/tenantSplitter/internals/cpuload
["driving cpu usage"]
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   96%/50%   2         10        2          14m
# we're at 96% cpu usage and have a limit of 50% so we should scale out
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-7c5458465c-2j5qg   1/1     Running   0          28m
baldur-deployment-7c5458465c-955mj   1/1     Running   0          28m
baldur-deployment-7c5458465c-fjlz9   1/1     Running   0          2m1s
baldur-deployment-7c5458465c-wxt6k   1/1     Running   0          2m1s
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   50%/50%   2         10        4          19m
# we've scaled out enough to drop the cpu usage to 50%

# now let's take the load off the cpu
$ curl http://10.240.11.54:8080/tenantSplitter/internals/cpuunload
["dropping cpu usage"]
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   0%/50%    2         10        4          21m
# wait a few minutes
$ kubectl get hpa
NAME                REFERENCE                      TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
baldur-autoscaler   Deployment/baldur-deployment   0%/50%    2         10        2          26m
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-7c5458465c-2j5qg   1/1     Running   0          38m
baldur-deployment-7c5458465c-955mj   1/1     Running   0          38m
# we've dropped back down to 2 pods
```
#### HPA with custom metrics
The environment in our container:
```sh
"KUBERNETES_PORT_443_TCP=tcp://172.20.0.1:443",
"PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
"KUBERNETES_PORT_443_TCP_ADDR=172.20.0.1",
"HOSTNAME=baldur-deployment-5644bc9fd5-krwx8",
"LD_LIBRARY_PATH=/usr/lib/jvm/java-1.8-openjdk/jre/lib/amd64/server:/usr/lib/jvm/java-1.8-openjdk/jre/lib/amd64:/usr/lib/jvm/java-1.8-openjdk/jre/../lib/amd64",
"KUBERNETES_PORT=tcp://172.20.0.1:443",
"KUBERNETES_PORT_443_TCP_PORT=443",
"KUBERNETES_PORT_443_TCP_PROTO=tcp",
"KUBERNETES_SERVICE_PORT_HTTPS=443",
"KUBERNETES_SERVICE_HOST=172.20.0.1",
"HOME=/home/sassrv",
"KUBERNETES_SERVICE_PORT=443"
```
Our pod name is stored in the environment variable HOSTNAME.

This is what gets added by kubernetes (pod was our custom field):
```
baldur_dog_treats_eaten{app="baldur",instance="10.240.5.20:8080",job="kubernetes-pods",kubernetes_namespace="default",kubernetes_pod_name="baldur-deployment-5644bc9fd5-n8qb7",pod="baldur-deployment",pod_template_hash="5644bc9fd5"}
```

#### Choosing Resource Requests
We need to set our resource requests as close as possible to the actual utilization of these resources. If the value is too low then we'll get throttled, impacting performance. If it's too high then we'll reserve unused capacity and waste money.

We can use a tool like [kube-resource-report](https://github.com/hjacobs/kube-resource-report) to determine the right values for our resource requests.

#### Other Scaling Criteria
There is also a tool named [kube-downscaler](https://github.com/hjacobs/kube-downscaler) that can scale resources in and out based on **time of day**.

### StatefulSets
A StatefulSet is used to manage stateful applications. Like a Deployment, it manages the deployment and scaling of a set of Pods that are based on an indentical container spec, but it also *provides guarantees about the ordering and uniqueness* of these Pods. These Pods are created from the same spec but are not interchangeable: each has a persistent identifier that it maintains across any rescheduling.

Some examples of applications that are identical but need to be scaled and have unique IDs are ZooKeeper and Kafka Connect.

A checklist of application attributes requirements that would necessitate using a StatefulSet are:
- stable unique network identifiers
- stable persistent storage
- ordered deployment and scaling
- ordered automated rolling updates

### DaemonSets
DaemonSets schedule a **single Pod on every node** in the cluster. This is used to land some sort of cross-application agent or daemon on each node in the cluster. Log collectors and monitoring agents are typically deployed as DaemonSets.

DaemonSets can target a subset of your nodes using label selectors. In our baldur-eks cluster we have two sets of worker nodes, a stateless set and a stateful set:

```sh
razing@mlb727 deployments $ kubectl get nodes -l alpha.eksctl.io/nodegroup-name=ng-stateful
NAME                           STATUS   ROLES    AGE     VERSION
ip-10-240-10-22.ec2.internal   Ready    <none>   7h42m   v1.14.6-eks-5047ed
ip-10-240-14-30.ec2.internal   Ready    <none>   7h42m   v1.14.6-eks-5047ed
ip-10-240-15-40.ec2.internal   Ready    <none>   7h42m   v1.14.6-eks-5047ed
ip-10-240-8-163.ec2.internal   Ready    <none>   7h42m   v1.14.6-eks-5047ed
razing@mlb727 deployments $ kubectl get nodes -l alpha.eksctl.io/nodegroup-name=ng-stateless
NAME                            STATUS   ROLES    AGE     VERSION
ip-10-240-14-108.ec2.internal   Ready    <none>   7h42m   v1.14.6-eks-5047ed
ip-10-240-15-45.ec2.internal    Ready    <none>   7h42m   v1.14.6-eks-5047ed
ip-10-240-26-203.ec2.internal   Ready    <none>   7h42m   v1.14.6-eks-5047ed
ip-10-240-29-4.ec2.internal     Ready    <none>   7h42m   v1.14.6-eks-5047ed
```
If we wanted a DaemonSet to just place Pods on our stateful nodes we'd use a nodeSelector with alpha.eksctl.io/nodegroup-name=ng-stateful.

### Jobs

### CronJob

## Services
A Service is used to expose an application running on a set of Pods as a single, load-balanced, network service. The Service is given a cluster IP and DNS address and load-balances requests to this address across the underlying Pods, keeping track of Pod deletion and creation.

Services are also used to facilitate Pod-to-Pod communication. Each Pod get a unique IP address but Pods scale in and out dynamically. We can front the Pods with a Service and use the Service endpoint to find the Pods as they migrate around the cluster.

We still have four Pods running the baldur-tenant splitter application from our Deployment in a previous section (if not rerun the deployment-rollout.yaml file). They are labeled with **app=baldur**.
```sh
$ kubectl get pods -l app=baldur
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-785975f6f4-2mwxt   1/1     Running   0          16h
baldur-deployment-785975f6f4-6hphg   1/1     Running   0          16h
baldur-deployment-785975f6f4-mbrmn   1/1     Running   0          16h
baldur-deployment-785975f6f4-zm2z2   1/1     Running   0          16h
```

In the following we'll create a Service that will route traffic to all Pods with the label app=baldur.

```sh
$ cat service-alb.yaml
apiVersion: v1
kind: Service
metadata:
  name: baldur-tensplit
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-internal: "true"
    service.beta.kubernetes.io/aws-load-balancer-extra-security-groups: "sg-06fbb55190b56b931"
    service.beta.kubernetes.io/aws-load-balancer-type: "alb"
spec:
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  selector:
    app: baldur
  type: LoadBalancer

$ kubectl apply -f service-alb.yaml
service/baldur-tensplit created

$ kubectl describe service baldur-tensplit
Name:                     baldur-tensplit
Namespace:                ci360
Labels:                   <none>
Annotations:              kubectl.kubernetes.io/last-applied-configuration:
                          service.beta.kubernetes.io/aws-load-balancer-extra-security-groups: sg-06fbb55190b56b931
                          service.beta.kubernetes.io/aws-load-balancer-internal: true
                          service.beta.kubernetes.io/aws-load-balancer-type: alb
Selector:                 app=baldur
Type:                     LoadBalancer
IP:                       172.20.126.39
LoadBalancer Ingress:     internal-a7b2ed8641c2711ea833212900007cf7-291583277.us-east-1.elb.amazonaws.com
Port:                     <unset>  80/TCP
TargetPort:               8080/TCP
NodePort:                 <unset>  31255/TCP
Endpoints:                10.240.29.62:8080,10.240.8.150:8080,10.240.8.249:8080 + 1 more...
Session Affinity:         None
External Traffic Policy:  Cluster
Events:
  Type    Reason                Age   From                Message
  ----    ------                ----  ----                -------
  Normal  EnsuringLoadBalancer  105s  service-controller  Ensuring load balancer
  Normal  EnsuredLoadBalancer   99s   service-controller  Ensured load balancer
```

Above we can see that we've created a Service that created an AWS ALB with a DNS name of internal-a7b2ed8641c2711ea833212900007cf7-291583277.us-east-1.elb.amazonaws.com. This ALB uses an existing security group (sg-06fbb55190b56b931) to control ingress/egress and balances traffic across our four Pods (the Endpoints section)

We can hit one of the Pods directly with:
```sh
$ curl http://10.240.29.62:8080/tenantSplitter/commons/ping
OK
```
But this just gives us access to one of the four Pods and this IP will change as Pods scale in and out.

If we hit the DNS name and port of the load balancer requests will be be sent, in a round robin fashion, to the underlying Pods.

```sh
curl http://internal-a7b2ed8641c2711ea833212900007cf7-291583277.us-east-1.elb.amazonaws.com:80/tenantSplitter/commons/ping
```

```sh
$ kubectl delete -f shell.yaml
pod "busybox" deleted

$ cat shell.yaml
apiVersion: v1
kind: Pod
metadata:
  name: ubuntu
spec:
  containers:
  - name: ubuntu
    image: ubuntu
    command: ["/bin/sh"]
    args: ["-c", "while true; do echo $(date -u) >> out.txt; sleep 60; done"]
$ kubectl apply -f shell.yaml
pod/ubuntu created
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-785975f6f4-2mwxt   1/1     Running   0          16h
baldur-deployment-785975f6f4-6hphg   1/1     Running   0          16h
baldur-deployment-785975f6f4-mbrmn   1/1     Running   0          16h
baldur-deployment-785975f6f4-zm2z2   1/1     Running   0          16h
ubuntu                               1/1     Running   0          5s

```
### Service Discovery
If we need to communicate between our services we can register our ALB's DNS name in some service discovery store and then have our dependent services look it up. If our service and the target service are both native kubernetes applications then this becomes even easier. Kubernetes has built in service discovery. Every Service object get a DNS entry in the Kubernetes DNS server that looks like this: **<service-name>.<namespace>.svc.<basedomain>**.

By default the basedomain is cluster.local but this can be changed. The namespace we're using here is ci360 so our baldur-tensplit service would be registered as: **baldur-tensplit.ci360.svc.cluster.local**.

This is very handy. All we need to know is the name of the service we need to hit, which should never change. As we migrate from environment to environment which just have to change the namespace to match the environment we're running in. So we can look up our baldur-tensplit application across our development, test, and production environments as:

- baldur-tensplit.dev.svc.cluster.local
- baldur-tensplit.tst.svc.cluster.local
- baldur-tensplit.eurc.svc.cluster.local
- baldur-tensplit.prod.svc.cluster.local

Let's check this out. I'll create another application in the cluster that just runs ubuntu pinging the date into a text file every minute. I'll log onto the container and hit our baldur-tensplit service's *commons/ping* endpoint using Kubernetes internal DNS name along with the port we exposed for the service (curl http://baldur-tensplit.ci360.svc.cluster.local:80/tenantSplitter/commons/ping).

```sh
$ cat shell.yaml
apiVersion: v1
kind: Pod
metadata:
  name: ubuntu
spec:
  containers:
  - name: ubuntu
    image: ubuntu
    command: ["/bin/sh"]
    args: ["-c", "while true; do echo $(date -u) >> out.txt; sleep 60; done"]
$ kubectl apply -f shell.yaml
$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
baldur-deployment-785975f6f4-2mwxt   1/1     Running   0          16h
baldur-deployment-785975f6f4-6hphg   1/1     Running   0          16h
baldur-deployment-785975f6f4-mbrmn   1/1     Running   0          16h
baldur-deployment-785975f6f4-zm2z2   1/1     Running   0          16h
ubuntu                               1/1     Running   0          5m26s
$ kubectl exec -it ubuntu /bin/bash
# install curl ...
root@ubuntu:/# curl http://baldur-tensplit.ci360.svc.cluster.local:80/tenantSplitter/commons/ping
OK
```
Looking good! All we need to do to find other services is know their service name and the endpoint we want, which should never change, and the environment we're running in (==kubernetes namespace), which we will have injected into our application as an environment variable (==StackPrefix). This means we don't have to register and lookup or services anymore, we'll just know where they are.

### Dealing with Failure
Earlier on we configured the Deployment for baldur-tenantsplitter to use liveness and readiness probes. If the liveness probe fails the Pod is restarted. If the readiness probe fails the Pod will continue to run but the Service will take it out of the load balancing pool. This means traffic won't get routed to the non-functional Pod but it remains in place so that we can trouble shoot it.

## ConfigMaps and Secrets
We want compile our application into a Docker image once and use it across all of our environments so that we're sure we're testing the exact same code that we're releasing to production. We do this by injecting configuration information via environment variables.

### ConfigMaps
ConfigMaps are objects that store key=value pairs of data in Kubernetes. We can use ConfigMaps to inject environment-specific configuration data into our applications (they're a bit more complicated than this but this will be our base usage).

We can supply our ConfigMaps with custom data for our different environments much like we currently do with our *environments.yaml* file.

Below I'll deploy our tenant-splitter application to a **dev** environment and a **prod** environment and I'll use config map data to customize the application for each environment. This is a bit contrived as I'm creating a dev-config.yaml file and a prod-config.yaml file to create the different values for the environments. Our real build would be populating the data for the config.yaml file from something like *environments.yaml*.

Below we'll generate the ConfigMap named baldur in our dev and prod environments:
```sh
# Make the dev config map
$ cat dev-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: baldur
  namespace: dev
data:
    baldur: "baldur in leisure wear"
    mymir: "mymir in leisure wear"
    butters_age: "12"
    butters_oldest: "true"
    color: "blue"
    temp: "cool"
$ kubectl apply -f dev-config.yaml
configmap/baldur created
$ kubectl get cm baldur -n dev
NAME     DATA   AGE
baldur   6      59m

# Make the prod config map
$ cat prod-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: baldur
  namespace: prod
data:
    baldur: "baldur in formal wear"
    mymir: "mymir in formal wear"
    butters_age: "12"
    butters_oldest: "true"
    color: "blue"
    temp: "cool"
$ kubectl apply -f prod-config.yaml
configmap/baldur created
$ kubectl get cm baldur -n prod
NAME     DATA   AGE
baldur   6      24s
```

Now we'll deploy our application to the dev and prod environments
```sh
$ cat baldur-pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
    env:
      - name: BALDUR_DOG
        valueFrom:
          configMapKeyRef:
            name: baldur
            key: baldur
      - name: MYMIR_DOG
        valueFrom:
          configMapKeyRef:
            name: baldur
            key: mymir

# deploy to dev
$ kubectl apply -f baldur-pod.yaml --namespace=dev
pod/baldur-tensplit created
$ kubectl get pod baldur-tensplit -n dev
NAME              READY   STATUS    RESTARTS   AGE
baldur-tensplit   1/1     Running   0          21s

# deploy to prod
$ kubectl apply -f baldur-pod.yaml --namespace=prod
pod/baldur-tensplit created
$ kubectl get pod baldur-tensplit -n prod
NAME              READY   STATUS              RESTARTS   AGE
baldur-tensplit   0/1     ContainerCreating   0          7s
```
We now have the baldur-tensplit application running in our production and development environments.

Let's grab their **environment variables** to make sure they are configured properly.

Development environment:
```sh
$ kubectl port-forward baldur-tensplit 8080:8080 -n dev
$ curl http://localhost:8080/tenantSplitter/internals/getenv
["KUBERNETES_PORT_443_TCP=tcp://172.20.0.1:443","PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin","KUBERNETES_PORT_443_TCP_ADDR=172.20.0.1","KUBERNETES_PORT=tcp://172.20.0.1:443","KUBERNETES_PORT_443_TCP_PROTO=tcp","KUBERNETES_SERVICE_HOST=172.20.0.1","KUBERNETES_SERVICE_PORT=443","HOSTNAME=baldur-tensplit","LD_LIBRARY_PATH=/usr/lib/jvm/java-1.8-openjdk/jre/lib/amd64/server:/usr/lib/jvm/java-1.8-openjdk/jre/lib/amd64:/usr/lib/jvm/java-1.8-openjdk/jre/../lib/amd64","MYMIR_DOG=mymir in leisure wear","KUBERNETES_PORT_443_TCP_PORT=443","KUBERNETES_SERVICE_PORT_HTTPS=443","BALDUR_DOG=baldur in leisure wear","HOME=/home/sassrv"]
# or just logging in and checking the environment:
$ kubectl exec -it -n dev baldur-tensplit /bin/bash
bash-4.4$ env
BALDUR_DOG=baldur in leisure wear
HOSTNAME=baldur-tensplit
KUBERNETES_PORT_443_TCP_PROTO=tcp
KUBERNETES_PORT_443_TCP_ADDR=172.20.0.1
MYMIR_DOG=mymir in leisure wear
KUBERNETES_PORT=tcp://172.20.0.1:443
PWD=/
HOME=/home/sassrv
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP=tcp://172.20.0.1:443
TERM=xterm
SHLVL=1
KUBERNETES_SERVICE_PORT=443
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
KUBERNETES_SERVICE_HOST=172.20.0.1
_=/usr/bin/env
```
We can see that
- "MYMIR_DOG=mymir in leisure wear"
- "BALDUR_DOG=baldur in leisure wear"
which is good, they should be casual in the dev environment.

Production environment:
```sh
$ kubectl port-forward baldur-tensplit 8080:8080 -n prod
$ curl http://localhost:8080/tenantSplitter/internals/getenv
["KUBERNETES_PORT_443_TCP=tcp://172.20.0.1:443","PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin","KUBERNETES_PORT_443_TCP_ADDR=172.20.0.1","KUBERNETES_PORT=tcp://172.20.0.1:443","KUBERNETES_PORT_443_TCP_PROTO=tcp","KUBERNETES_SERVICE_HOST=172.20.0.1","KUBERNETES_SERVICE_PORT=443","HOSTNAME=baldur-tensplit","LD_LIBRARY_PATH=/usr/lib/jvm/java-1.8-openjdk/jre/lib/amd64/server:/usr/lib/jvm/java-1.8-openjdk/jre/lib/amd64:/usr/lib/jvm/java-1.8-openjdk/jre/../lib/amd64","MYMIR_DOG=mymir in formal wear","KUBERNETES_PORT_443_TCP_PORT=443","KUBERNETES_SERVICE_PORT_HTTPS=443","BALDUR_DOG=baldur in formal wear","HOME=/home/sassrv"]
```
We can see that
- "MYMIR_DOG=mymir in formal wear"
- "BALDUR_DOG=baldur in formal wear"
which is as it should be in a production environment.

So we've deployed the exact same image, using the same yaml file to two different environments and it gets automatically configured properly for the environment that it's in.

### Secrets
ConfigMaps are great for configuration data that doesn't have to be secure but what about things like database passwords.
Anyone with access to the Kubernetes cluster can read this data:
```sh
$ kubectl describe cm baldur -n dev
Name:         baldur
Namespace:    dev
Data
====
butters_age:
----
12
butters_oldest:
----
true
color:
----
blue
mymir:
----
mymir in leisure wear
temp:
----
cool
baldur:
----
baldur in leisure wear
Events:  <none>
```

Let's create a secret and feed that in as an environment variable:
```sh
# Obviously we'll want to create the secret in a secure way so that it's not sitting in git
$ cat dev-butters-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: butters
type: Opaque
data:
  butters: YnV0dGVycyBpbiBraGFraXM=
$ kubectl apply -f dev-butters-secret.yaml -n dev
secret/butters created
razing@mlb727 config (master) $ kubectl get secrets -n dev
NAME                  TYPE                                  DATA   AGE
butters               Opaque                                1      27s
default-token-6wbld   kubernetes.io/service-account-token   3      3h44m
razing@mlb727 config (master) $ kubectl describe secret butters -n dev
Name:         butters
Namespace:    dev
Labels:       <none>
Annotations:
Type:         Opaque

Data
====
butters:  17 bytes
# unlike our ConfigMap we can't actually see what the value is for the butters key
```
Good, we've stored the secret but we can't see what the value is - it's Opaque.

Now let's use the secret in our Pod:
```sh
$ cat baldur-pod-secret.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-tensplit
spec:
  containers:
  - name: baldurpod
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
    env:
      - name: BALDUR_DOG
        valueFrom:
          configMapKeyRef:
            name: baldur
            key: baldur
      - name: MYMIR_DOG
        valueFrom:
          configMapKeyRef:
            name: baldur
            key: mymir
      - name: BUTTERS_DOG
        valueFrom:
          secretKeyRef:
            name: butters
            key: butters
$ kubectl apply -f baldur-pod-secret.yaml -n dev
pod/baldur-tensplit created

$ kubectl port-forward baldur-tensplit 8080:8080 -n dev
Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
$ curl http://localhost:8080/tenantSplitter/internals/getenv
["KUBERNETES_PORT_443_TCP=tcp://172.20.0.1:443","PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin","KUBERNETES_PORT_443_TCP_ADDR=172.20.0.1","KUBERNETES_PORT=tcp://172.20.0.1:443","KUBERNETES_PORT_443_TCP_PROTO=tcp","KUBERNETES_SERVICE_HOST=172.20.0.1","KUBERNETES_SERVICE_PORT=443","HOSTNAME=baldur-tensplit","LD_LIBRARY_PATH=/usr/lib/jvm/java-1.8-openjdk/jre/lib/amd64/server:/usr/lib/jvm/java-1.8-openjdk/jre/lib/amd64:/usr/lib/jvm/java-1.8-openjdk/jre/../lib/amd64","MYMIR_DOG=mymir in leisure wear","BUTTERS_DOG=butters in khakis","KUBERNETES_PORT_443_TCP_PORT=443","KUBERNETES_SERVICE_PORT_HTTPS=443","BALDUR_DOG=baldur in leisure wear","HOME=/home/sassrv"]
```

And there we have it:
- "BUTTERS_DOG=butters in khakis"

If we wanted to make this even more secure, we could have put an encoded form of the BUTTERS_DOG value in the secret and stored that in the container's environment variable. We could then run have the application pass it through a decoder (like our PasswordDecoder lambda function) before using it in the application.

## Storage
We'll look at three types of file-system based storage here:
- ephemeral storage on container file system
- ebs-based storage
- efs-based storage

### Ephemeral Storage on Container File System
Your container has a linux file system and you can store stuff on it if you need to cache thing temporarily. This file system **will go away on container restart**.

We don't have to do anything special to use this file system, just spin up our Pod and use it.

We'll use a ReplicaSet to create our Pod so that we can easily scale out to more instances:

```sh
$ cat container-ephemeral.yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: baldur-rs
spec:
  replicas: 1
  selector:
    matchLabels:
      app: baldur-tensplit
  template:
    metadata:
      labels:
        app: baldur-tensplit
    spec:
      containers:
      - name: baldur
        image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir

$ kubectl apply -f container-ephemeral.yaml
replicaset.apps/baldur-rs created

# We've created a replica set that create one pod
$ kubectl get rs
NAME        DESIRED   CURRENT   READY   AGE
baldur-rs   1         1         1       25s
$ kubectl get pods
NAME              READY   STATUS    RESTARTS   AGE
baldur-rs-6lftp   1/1     Running   0          32s

# Let's allow connections on localhost port 8080
$ kubectl port-forward baldur-rs-6lftp 8080:8080
Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080

# Now we can hit the quotes endpoint which stores quotes on disk
$ curl http://localhost:8080/tenantSplitter/files/quotes
["It was the best of times it was the worst of times.","Life is never fair, and perhaps it is a good thing for most of us that it is not.","You can't always get what you want (MJ)."]

# We'll add a new quote, using the UI and then query the list of quotes again - you can see below that we have our fourth quote
$ curl http://localhost:8080/tenantSplitter/files/quotes
["It was the best of times it was the worst of times.","Life is never fair, and perhaps it is a good thing for most of us that it is not.","You can't always get what you want (MJ).","Baldur is a good dog"]

# And yes this is being stored on the container's file system
$ kubectl exec -it baldur-rs-6lftp /bin/bash
bash-4.4$ cat /data/mkt-devops/mkt-tenant-splitter/quotes/quotes.txt
It was the best of times it was the worst of times.
Life is never fair, and perhaps it is a good thing for most of us that it is not.
You cant always get what you want (MJ).
Baldur is a good dog
```

We happens if we spin the pod down and back up?

```sh
$ kubectl scale rs baldur-rs --replicas=0
replicaset.extensions/baldur-rs scaled
$ kubectl scale rs baldur-rs --replicas=1
replicaset.extensions/baldur-rs scaled
$ kubectl get pods
NAME              READY   STATUS    RESTARTS   AGE
baldur-rs-pxfkv   1/1     Running   0          5s
$ kubectl exec -it baldur-rs-pxfkv /bin/bash
bash-4.4$ cat /data/mkt-devops/mkt-tenant-splitter/quotes/quotes.txt
It was the best of times it was the worst of times.
Life is never fair, and perhaps it is a good thing for most of us that it is not.
You cant always get what you want (MJ).
# We don't have our fourth quote anymore - not surprising, as it was stored on the original container's local file system.
```
If we had scaled out to two pods we would have seen the same thing. The second pod wouldn't have had the added quote as it would be reading it's only local file system and the new quote is on the first containers local filesystem.

So the local container filesystem is a good place to store temporary data that doesn't need to be shared between the instances of your service and doesn't need to persist between Pod restarts.

### EBS Based Storage

Our Kubernetes cluster has the Amazon EBS CSI Driver installed. This lets us attach an EBS volume to our Pod.

Note you **most likely won't be using this for your service**. If you have **Pods that autoscale this won't work** as the EBS volume can only be attached to one Pod at a time and is confined to the availability zone it's created in.

This does work well for things like Kafka brokers, PostgreSQL databases, etc that require a hard link between the application and disk and require data to be preserved between disk restarts.

#### StorageClass
In the following I'll create a storage class that our Pod will use to request an EBS volume. Application developers won't need to do this as it will be created by the Kubernetes cluster administrator.

```sh
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
$ kubectl describe sc ebs-gp2-ext4
Name:            ebs-gp2-ext4
IsDefaultClass:  No
Annotations:     kubectl.kubernetes.io/last-applied-configuration={"allowVolumeExpansion":true,"apiVersion":"storage.k8s.io/v1","kind":"StorageClass","metadata":{"annotations":{},"name":"ebs-gp2-ext4"},"parameters":{"fsType":"ext4","type":"gp2"},"provisioner":"ebs.csi.aws.com","reclaimPolicy":"Delete","volumeBindingMode":"WaitForFirstConsumer"}

Provisioner:           ebs.csi.aws.com
Parameters:            fsType=ext4,type=gp2
AllowVolumeExpansion:  True
MountOptions:          <none>
ReclaimPolicy:         Delete
VolumeBindingMode:     WaitForFirstConsumer
Events:                <none>
```
The above creates a storage class (**ebs-gp2-ext4**) that we can use to provision EBS volumes. We've configured the StorageClass with the following:
- volumeBindingMode: WaitForFirstConsumer (volumes are not provisioned until a pod makes a persistent volume claim, allowed values: Immediate, WaitForFirstConsumer)
- reclaimPolicy: Delete (allowed values: Retain=retain volume when Persistent Volume Claim (PVC) is deleted, Delete=delete volume when PVC is deleted)
- type: gp2 (EBS volume type: can be io1, gp2, sc1, st1, standard)
- fsType: ext4 (File system type that will be formatted during volume creation: can be xfs, ext2, ext3, ext4)
- allowVolumeExpansion: true (let's you change the disk size by editing the PVC)

#### PersistentVolumeClaim
Now we have to create a PersistentVolumeClaim that fills out the instance details for our volume. In particular:
- volumeMode: Filesystem (vs block storage) - for Filesystems, the file system type will be determined by the storage class.
- storage: 4Gi (disk size)
```sh
$ cat ebs-pvc.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: baldur-ebs-claim
  annotations:
    name: baldur-pvc
spec:
  accessModes:
    - ReadWriteOnce
  volumeMode: Filesystem
  resources:
    requests:
      storage: 4Gi
  storageClassName: ebs-gp2-ext4
$ kubectl apply -f ebs-pvc.yaml
persistentvolumeclaim/baldur-ebs-claim created
$ kubectl get pvc
NAME               STATUS    VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   AGE
baldur-ebs-claim   Pending                                      ebs-gp2-ext4   7s
$ kubectl describe pvc baldur-ebs-claim
Name:          baldur-ebs-claim
Namespace:     ci360
StorageClass:  ebs-gp2-ext4
Status:        Pending
Volume:
Labels:        <none>
Annotations:   kubectl.kubernetes.io/last-applied-configuration:
                 {"apiVersion":"v1","kind":"PersistentVolumeClaim","metadata":{"annotations":{"name":"baldur-pvc"},"name":"baldur-ebs-claim","namespace":"c...
               name: baldur-pvc..."
Finalizers:    [kubernetes.io/pvc-protection]
Capacity:
Access Modes:
VolumeMode:    Filesystem
Mounted By:    <none>
Events:
  Type    Reason                Age               From                         Message
  ----    ------                ----              ----                         -------
  Normal  WaitForFirstConsumer  3s (x4 over 22s)  persistentvolume-controller  waiting for first consumer to be created before binding
```
Above we've created our PVC for a 4GB gp2 EBS volume with an ext4 filesystem. The disk isn't actually created yet because we have set the VolumeBindingMode to WaitForFirstConsumer. We need to do this because the EBS volume has to be spun up in the same availability zone that the Pod is created in. If it's not it can't be attached.

#### Application with volumeMounts
Let's use our pvc in an application:
```sh
$ cat ebs-based-app.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-ebs-pod
spec:
  initContainers:
  - name: volume-permissions-hack
    image: busybox
    command: ["/bin/chmod","-R","a+rw", "/data"]
    volumeMounts:
    - name: data-volume
      mountPath: /data
  containers:
  - name: baldur-ebs-app
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
    imagePullPolicy: Always
    volumeMounts:
    - mountPath: /data
      name: data-volume
  volumes:
  - name: data-volume
    persistentVolumeClaim:
      claimName: baldur-ebs-claim
$ kubectl apply -f ebs-based-app.yaml
pod/baldur-ebs-pod created
$ kubectl get pods -o wide
NAME             READY   STATUS    RESTARTS   AGE   IP             NODE                            NOMINATED NODE   READINESS GATES
baldur-ebs-pod   1/1     Running   0          17m   10.240.15.58   ip-10-240-11-193.ec2.internal   <none>           <none>

#Now our volume should move from pending to bound
$ kubectl get pvc
NAME               STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
baldur-ebs-claim   Bound    pvc-ea297168-2115-11ea-866e-0af77482d1d1   4Gi        RWO            ebs-gp2-ext4   2m6s
```
If we look the volume up in aws we can see that it's attached to one of our worker nodes in our baldur-eks-ng-stateful-Node group (more on this later), and is in the state: in-use.

If we describe our pod we can see the name of the volume that was created and attached (showing only relevant parts):
```sh
$ kubectl describe pod baldur-ebs-pod
Name:         baldur-ebs-pod
Namespace:    ci360
Containers:
  baldur-ebs-app:
    Mounts:
      /data from data-volume (rw)
Volumes:
  data-volume:
    Type:       PersistentVolumeClaim (a reference to a PersistentVolumeClaim in the same namespace)
    ClaimName:  baldur-ebs-claim
    ReadOnly:   false

Events:
  Type     Reason                  Age                    From                                    Message
  ----     ------                  ----                   ----                                    -------
  Normal   Scheduled               9m58s                  default-scheduler                       Successfully assigned ci360/baldur-ebs-pod to ip-10-240-11-193.ec2.internal
  Warning  FailedAttachVolume      9m57s (x2 over 9m58s)  attachdetach-controller                 AttachVolume.Attach failed for volume "pvc-ea297168-2115-11ea-866e-0af77482d1d1" : rpc error: code = AlreadyExists desc = Resource already exists
  Normal   SuccessfulAttachVolume  9m56s                  attachdetach-controller                 AttachVolume.Attach succeeded for volume "pvc-ea297168-2115-11ea-866e-0af77482d1d1"
  Normal   Pulling                 9m40s                  kubelet, ip-10-240-11-193.ec2.internal  Pulling image "busybox"
  Normal   Pulled                  9m40s                  kubelet, ip-10-240-11-193.ec2.internal  Successfully pulled image "busybox"
  Normal   Created                 9m40s                  kubelet, ip-10-240-11-193.ec2.internal  Created container volume-permissions-hack
  Normal   Started                 9m40s                  kubelet, ip-10-240-11-193.ec2.internal  Started container volume-permissions-hack
  Normal   Pulling                 9m39s                  kubelet, ip-10-240-11-193.ec2.internal  Pulling image "952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir"
  Normal   Pulled                  9m39s                  kubelet, ip-10-240-11-193.ec2.internal  Successfully pulled image "952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir"
  Normal   Created                 9m39s                  kubelet, ip-10-240-11-193.ec2.internal  Created container baldur-ebs-app
  Normal   Started                 9m39s                  kubelet, ip-10-240-11-193.ec2.internal  Started container baldur-ebs-app

  # Check out the volume details:
  $ aws ec2 describe-volumes --volume-ids "vol-0a6c7b3ec151b271b"
  {
    "Volumes": [
        {
            "Attachments": [
                {
                    "AttachTime": "2019-12-17T21:43:05.000Z",
                    "Device": "/dev/xvdba",
                    "InstanceId": "i-063aeb09fe05cb844",
                    "State": "attached",
                    "VolumeId": "vol-0a6c7b3ec151b271b",
                    "DeleteOnTermination": false
                }
            ],
            "AvailabilityZone": "us-east-1b",
            "CreateTime": "2019-12-17T21:42:58.277Z",
            "Encrypted": false,
            "Size": 4,
            "SnapshotId": "",
            "State": "in-use",
            "VolumeId": "vol-0a6c7b3ec151b271b",
            "Iops": 100,
            "Tags": [
                {
                    "Key": "resourceowner",
                    "Value": "i-05a5a0e497c796e8c"
                },
                {
                    "Key": "CSIVolumeName",
                    "Value": "pvc-ea297168-2115-11ea-866e-0af77482d1d1"
                }
            ],
            "VolumeType": "gp2"
        }
    ]
}
```

Let's create some data. We'll port-forward to our application, and use the UI to create a couple of new quotes:
- Baldur is a good dog
- Mymir is a good dog

```sh
$ kubectl port-forward baldur-ebs-pod 8080:8080
Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
# Use the UI to add two quotes - Baldur & Mymir
# Grab the full list from the REST endpoint
$ curl http://localhost:8080/tenantSplitter/files/quotes
["It was the best of times it was the worst of times.","Life is never fair, and perhaps it is a good thing for most of us that it is not.","You can't always get what you want (MJ).","Baldur is a good dog","Mymir is a good dog"]
```

What happens when we delete our application?
```sh
razing@mlb727 mkt-template-docker (staging) $ kubectl delete pod baldur-ebs-pod
pod "baldur-ebs-pod" deleted
razing@mlb727 mkt-template-docker (staging) $ kubectl get pvc
NAME               STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
baldur-ebs-claim   Bound    pvc-a1617657-21a5-11ea-8332-12900007cf77   4Gi        RWO            ebs-gp2-ext4   26m
```
Our PVC shows up as being bound still but if we check the aws console we see that the EBS still exists but has been detached for the EC2 worker node and is in the state: available.

Let's re-create our Pod:
```sh
$ kubectl apply -f ebs-based-app.yaml
pod/baldur-ebs-pod created
$ kubectl get pods
NAME             READY   STATUS     RESTARTS   AGE
baldur-ebs-pod   0/1     Init:0/1   0          8s
$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
baldur-ebs-pod   1/1     Running   0          28s
$ kubectl port-forward baldur-ebs-pod 8080:8080
$ curl http://localhost:8080/tenantSplitter/files/quotes
["It was the best of times it was the worst of times.","Life is never fair, and perhaps it is a good thing for most of us that it is not.","You can't always get what you want (MJ).","Baldur is a good dog","Mymir is a good dog"]
```
We can see that we still have the two quotes that we added with the previous Pod. This is good! We can keep our data between restarts or upgrades of our Pods!

Now let's delete our Pod and our PVC:
```sh
$ kubectl delete pod baldur-ebs-pod
pod "baldur-ebs-pod" deleted
$ kubectl delete pvc baldur-ebs-claim
persistentvolumeclaim "baldur-ebs-claim" deleted
```
Now we look for the volume in the aws console we can see that it's gone.

Let's spin everything back up:
```sh
$ kubectl apply -f ebs-pvc.yaml
persistentvolumeclaim/baldur-ebs-claim created
$ kubectl apply -f ebs-based-app.yaml
pod/baldur-ebs-pod created
$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
baldur-ebs-pod   1/1     Running   0          23s
$ kubectl port-forward baldur-ebs-pod 8080:8080
Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
$ curl http://localhost:8080/tenantSplitter/files/quotes
["It was the best of times it was the worst of times.","Life is never fair, and perhaps it is a good thing for most of us that it is not.","You can't always get what you want (MJ)."]
```
Now we've lost our new quotes and are just have the canned data again.

If we had set ReclaimPolicy: Retain in our StorageClass and deleted the PVC then the EBS volume would be retained even if the PVC claim was deleted. It would be stranded though as re-creating the PVC claim and using it in a new Pod would not remount this disk but would create and use a new EBS volume.

#### Selecting the NodeGroup for Pod creation and Volume attachment
If you scroll up a bit, you'll see that our EBS volume was attached to the node **ip-10-240-11-193.ec2.internal**.

When the cluster was created it was setup with two nodegroups. One runs on spot instances and was given a label role=stateless. The other runs on on-demand instances and was given a label role=stateful.

Let's look at which node our Pod was created on:
```sh
$ kubectl get nodes -l role=stateful
NAME                            STATUS   ROLES    AGE     VERSION
ip-10-240-10-63.ec2.internal    Ready    <none>   3h43m   v1.14.6-eks-5047ed
ip-10-240-11-154.ec2.internal   Ready    <none>   3h43m   v1.14.6-eks-5047ed
ip-10-240-11-193.ec2.internal   Ready    <none>   3h43m   v1.14.6-eks-5047ed
ip-10-240-8-61.ec2.internal     Ready    <none>   3h43m   v1.14.6-eks-5047ed
$ kubectl get nodes -l role=stateless
NAME                            STATUS   ROLES    AGE     VERSION
ip-10-240-11-151.ec2.internal   Ready    <none>   3h44m   v1.14.6-eks-5047ed
ip-10-240-12-56.ec2.internal    Ready    <none>   3h44m   v1.14.6-eks-5047ed
ip-10-240-26-193.ec2.internal   Ready    <none>   3h44m   v1.14.6-eks-5047ed
ip-10-240-30-181.ec2.internal   Ready    <none>   3h43m   v1.14.6-eks-5047ed
```
It looks like our Pod was created on one of the stateful nodes which is good but was just lucky.

We need to force kubernetes to deploy this application to one of the nodes in our group that's labelled stateful because we want long-term stability for it.
We can do this by using a **nodeSelector** in our deployment description.

We have the label *role=stateful* on all of our non-spot instances. Let's ensure that our application is deployed to on of those nodes. We can do this by adding the nodeSelector: role: stateful section to our deployment. Once we do this all Pods created with this manifest will be deployed to one of the servers in the stateful group.
The modified deployment file is shown below:

```sh
$ cat ebs-stateful-app.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-ebs-pod
spec:
  initContainers:
  - name: volume-permissions-hack
    image: busybox
    command: ["/bin/chmod","-R","a+rw", "/data"]
    volumeMounts:
    - name: data-volume
      mountPath: /data
  containers:
  - name: baldur-ebs-app
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
    imagePullPolicy: Always
    volumeMounts:
    - mountPath: /data
      name: data-volume
  nodeSelector:
    role: stateful
  volumes:
  - name: data-volume
    persistentVolumeClaim:
      claimName: baldur-ebs-claim
```

### EFS Based Storage

Our Kubernetes cluster has the Amazon EFS CSI Driver installed. This lets us attach an EBS volume to our Pod. If you have a microservice that scales and need persistent disk storage that's shared across the scaled instances then this is what you should be using. In fact you should think about whether or not your data belongs in RDS/Dynamo/Redis first, and then move to EFS if you really need a file system.

This works a lot like EBS usage did above, but you'll be hooking up to a shared EFS-based disk rather than creating an EBS volume. This means:
- you can attach the disk to multiple Pods at once
- the Pods can be spun up in any availability zone (if your EFS disk is provisioned to properly span all of your subnets)
- you'll be **sharing the disk with other microservice** everyone will have to follow some root path convention.

#### Administrator Details
We can't just dynamically provision EFS like we did with the EBS volumes:
- The EFS resource needs to be pre-provisioned by an admin - the disk, security group, and mount points on the VPC subnets your k8s cluster runs in
- The EFS disk only has the root (/) directory and is only writable by the root user. The admin will have to mount the disk on a EC2 instance and *sudo chmod 777 /mntpoint* the disk for sassrv to use it. Permissions are by numeric ID so setting up sudo chown sassrv:sassrv /mntpoint won't work.
- more than one application can grab this disk - we'll have to use some convention like writing to **/{island}/{project}/data** to avoid messing up other application's data.
- We have to create a PersistentVolume pointed at the EFS disk since it's not automatically provisioned.

So up front the kubernetes admin will create the EFS disk in aws, and then create the proper StorageClass and PersistentVolume:
```sh
# Storage Class
$ cat efs-storageclass.yaml
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: efs-sc
provisioner: efs.csi.aws.com
reclaimPolicy: Retain
$ kubectl apply -f efs-storageclass.yaml
storageclass.storage.k8s.io/efs-sc created
$ kubectl get sc
NAME            PROVISIONER             AGE
ebs-gp2-ext4    ebs.csi.aws.com         143m
efs-sc          efs.csi.aws.com         12s

# PersistentVolume
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
$ kubectl apply -f efs-pv.yaml
persistentvolume/efs-pv created
$ kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM                                STORAGECLASS   REASON   AGE
efs-pv                                     5Gi        RWX            Retain           Available                                        efs-sc                  42s
```

#### Application Developer Details
As with EBS we need to create a PersistentVolumeClaim using our provisioned storage class (efs-sc), and then we need to use this PVC in our application deployment manifest:

```sh
# Create the PVC
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
$ kubectl apply -f efs-pvc.yaml
persistentvolumeclaim/efs-claim created
$ kubectl get pvc
NAME        STATUS   VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   AGE
efs-claim   Bound    efs-pv   5Gi        RWX            efs-sc         5s

# Use the PVC in our application
$ cat efs-based-app.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-efs-pod
spec:
  initContainers:
  containers:
  - name: baldur-efs-app
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
    imagePullPolicy: Always
    volumeMounts:
    - mountPath: /data
      name: data-volume
  volumes:
  - name: data-volume
    persistentVolumeClaim:
      claimName: efs-claim
$ kubectl apply -f efs-based-app.yaml
pod/baldur-efs-pod created
$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
baldur-efs-pod   1/1     Running   0          30s
$ kubectl port-forward baldur-efs-pod 8080:8080
Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
```

Once again we'll look at our quotes which get stored on disk. We can see that we start with our pre-canned quotes, and that if we add two new quotes they get persisted to disk:
```sh
$ curl http://localhost:8080/tenantSplitter/files/quotes
["It was the best of times it was the worst of times.","Life is never fair, and perhaps it is a good thing for most of us that it is not.","You can't always get what you want (MJ)."]
# Use the UI to add a Baldur quote and a Mymir quote
$ curl http://localhost:8080/tenantSplitter/files/quotes
["It was the best of times it was the worst of times.","Life is never fair, and perhaps it is a good thing for most of us that it is not.","You can't always get what you want (MJ).","Baldur is a good dog","Mymir is a good dog"]
```
Let's jump into our container and see what the disk looks like:
```sh
$ kubectl exec -it baldur-efs-pod /bin/bash
bash-4.4$ df -h
Filesystem                Size      Used Available Use% Mounted on
overlay                  50.0G      2.5G     47.5G   5% /
tmpfs                    64.0M         0     64.0M   0% /dev
tmpfs                     1.9G         0      1.9G   0% /sys/fs/cgroup
fs-9fc07b1e.efs.us-east-1.amazonaws.com:/
                          8.0E         0      8.0E   0% /data
```
The formatting is a bit wonky but we can see our mountpoint /data attached to an 8GB disk.

Let's spin up a simple linux application that echos the date to a file on our EFS disk:

```sh
$ cat busybox-efs.yaml
apiVersion: v1
kind: Pod
metadata:
  name: busybox
spec:
  containers:
  - name: busybox
    image: busybox
    command: ["/bin/sh"]
    args: ["-c", "while true; do echo $(date -u) >> /data/date.txt; sleep 5; done"]
    volumeMounts:
    - name: persistent-storage
      mountPath: /data
  volumes:
  - name: persistent-storage
    persistentVolumeClaim:
      claimName: efs-claim
$ kubectl apply -f busybox-efs.yaml
pod/busybox created
$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
baldur-efs-pod   1/1     Running   0          40m
busybox          1/1     Running   0          4s

# Let's jump onto the busybox
$ kubectl exec -it busybox /bin/sh
/ # df -h
Filesystem                Size      Used Available Use% Mounted on
overlay                  50.0G      2.5G     47.5G   5% /
tmpfs                    64.0M         0     64.0M   0% /dev
tmpfs                     1.9G         0      1.9G   0% /sys/fs/cgroup
fs-9fc07b1e.efs.us-east-1.amazonaws.com:/
                          8.0E         0      8.0E   0% /data

# Our busybox app should be writing data to /data/date.txt
/data # ls /data
date.txt    mkt-devops
/data # cat /data/date.txt
Wed Dec 18 20:21:05 UTC 2019
Wed Dec 18 20:21:11 UTC 2019
Wed Dec 18 20:21:16 UTC 2019
Wed Dec 18 20:21:21 UTC 2019
Wed Dec 18 20:21:26 UTC 2019
Wed Dec 18 20:21:31 UTC 2019
Wed Dec 18 20:21:36 UTC 2019
Wed Dec 18 20:21:41 UTC 2019
Wed Dec 18 20:21:46 UTC 2019
Wed Dec 18 20:21:51 UTC 2019
Wed Dec 18 20:21:56 UTC 2019

# And we can see that this is the same disk that our baldur-efs-pod is using
/ # cat /data/mkt-devops/mkt-tenant-splitter/quotes/quotes.txt
It was the best of times it was the worst of times.
Life is never fair, and perhaps it is a good thing for most of us that it is not.
You cant always get what you want (MJ).
Baldur is a good dog
Mymir is a good dog
```
This is why we need to agree on a convention for the base path for our application data. It would be pretty easy for one application to over-write another application's data if we don't.

We could spin up a new EFS disk for each application but that would be much more cumbersome and harder to maintain.

If we're using a common EFS disk we should:
- standardize on a root mount point like /data
- write to a path that includes our island and project name: /data/{island-name}/{project-name}/

Now we'll spin up a second instance of our badlur-efs-pod and call it baldur-efs-pod1:
```sh
# Create the second instance
$ cat efs-based-app1.yaml
apiVersion: v1
kind: Pod
metadata:
  name: baldur-efs-pod1
spec:
  initContainers:
  containers:
  - name: baldur-efs-app1
    image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:mymir
    imagePullPolicy: Always
    volumeMounts:
    - mountPath: /data
      name: data-volume
  volumes:
  - name: data-volume
    persistentVolumeClaim:
      claimName: efs-claim
$ kubectl apply -f efs-based-app1.yaml
pod/baldur-efs-pod1 created
# We can see below that we've spun up the new Pod on a different EC2 instance (NODE) than the first Pod
$ kubectl get pods -o wide
NAME              READY   STATUS    RESTARTS   AGE     IP              NODE                            NOMINATED NODE   READINESS GATES
baldur-efs-pod    1/1     Running   0          48m     10.240.8.162    ip-10-240-12-68.ec2.internal    <none>           <none>
baldur-efs-pod1   1/1     Running   0          83s     10.240.11.207   ip-10-240-11-113.ec2.internal   <none>           <none>
busybox           1/1     Running   0          8m53s   10.240.8.32     ip-10-240-15-26.ec2.internal    <none>           <none>

# Check out the quote data
razing@mlb727 storage (master) $ kubectl port-forward baldur-efs-pod1 8080:8080
Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
# We'll use the UI to store a quote about Butters
# Now lets check or quote endpoint:
$ curl http://localhost:8080/tenantSplitter/files/quotes
["It was the best of times it was the worst of times.","Life is never fair, and perhaps it is a good thing for most of us that it is not.","You can't always get what you want (MJ).","Baldur is a good dog","Mymir is a good dog","Butters is a good dog"]
# Great we have the Baldur and Mymir quotes that the baldur-efs-pod created and the new Butters quote that the baldur-efs-pod1 created
```
Looks good, our *manually* autoscaled group is sharing the same disk and data - all instances will have the same view of the application's shared data.

#### Fun with EFS
When we logged into our containers we saw that the EFS disk was mounted as an 8GB file system at the /data mount point. What happens if we dump a huge file here that requires more than 8GB of space?

Let's create a 10GB file:

```sh
$ curl http://localhost:8080/tenantSplitter/files/makebigfile?size=10
razing@mlb727 storage (master) $ kubectl exec -it baldur-efs-pod /bin/bash
bash-4.4$ df -h
Filesystem                Size      Used Available Use% Mounted on
overlay                  50.0G      2.6G     47.4G   5% /
tmpfs                    64.0M         0     64.0M   0% /dev
tmpfs                     1.9G         0      1.9G   0% /sys/fs/cgroup
fs-9fc07b1e.efs.us-east-1.amazonaws.com:/
                          8.0E         0      8.0E   0% /data
# Ok its actually an 8.0E disk size not 8.0GB (E not GB)
# Did we manage to create our 10GB file?
bash-4.4$ cd /data/mkt-devops/mkt-tenant-splitter/quotes/
bash-4.4$ ls -hl
total 10485764
-rw-r--r--    1 sassrv   sas        10.0G Dec 18 21:46 bigfile.txt
-rw-r--r--    1 sassrv   sas          245 Dec 18 21:10 quotes.txt
# E=Elastic! The EFS disk can grow elastically to petabyte-scale
```
This is why we just need one EFS volume for all of our applications. It makes setup and maintenance much easier and E=Elastic. The EFS disk can grow elastically to petabyte-scale and is spread across multiple storage servers enabling parallel access. We will never run out of room!

## Metrics
We've installed a Metric server in the kubernetes cluster. It gathers and stores a variety of cluster metrics. You can hit it at the control plane endpoint: https://DF08F33D88552F7657A214DDEFBCB8EE.gr7.us-east-1.eks.amazonaws.com/metrics (if you have the cluster's cert). Or you can hit it with kubectl:

```sh
razing@mlb727 storage (master) $ kubectl get --raw /metrics > metrics.txt
razing@mlb727 storage (master) $ wc metrics.txt
   14057   32216 2479300 metrics.txt
razing@mlb727 storage (master) $ tail -5 metrics.txt
workqueue_work_duration_seconds_bucket{name="crd_naming_condition_controller",le="1"} 6608
workqueue_work_duration_seconds_bucket{name="crd_naming_condition_controller",le="10"} 6608
workqueue_work_duration_seconds_bucket{name="crd_naming_condition_controller",le="+Inf"} 6608
workqueue_work_duration_seconds_sum{name="crd_naming_condition_controller"} 0.08529290899999978
workqueue_work_duration_seconds_count{name="crd_naming_condition_controller"} 6608
```
So in a fairly unused cluster we're gathering 14K+ metrics. This is a snapshot in time - we're usually also interested in trends, historical spikes, temporary outages, etc. Fortunately the /metric endpoint is scraped and the data is stored in the time-series database Prometheus. The Prometheus data can then be queried and graphed directly or fed into Grafana dashboards.

### Prometheus and Grafana

Service stuff:
https://kubernetes.io/docs/concepts/services-networking/service/
you can create a service without a Pod selector and then create the endpoint for the service manually -> use this to point the Kubernetes service name to an external IP to cross the Kubernetes / EC2 boundary!


We can get to our Prometheus and Grafana servers directly as follows (in our official cluster we'll front these with ALBs and DNS names):

```sh
# Prometheus
$ kubectl -n prometheus port-forward deployment/prometheus-server 9090
Forwarding from 127.0.0.1:9090 -> 9090
Forwarding from [::1]:9090 -> 9090
# Grafana
