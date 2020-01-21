# All Things Docker

## Base Docker

### Building Docker Images

log in to our Amazon Docker Registry (ECR)

    $ aws ecr get-login --no-include-email --region us-east-1 | /bin/bash

or Docker hub (docker.io registry)

    $ docker login --username mkt-devops --password mypass

build image from our Docker file

    $ docker build -t 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:1.0 .

push version 1.0 to our ECR repo

    $ docker push 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter

### Running Docker Containers

run the app

    $ export REPO=export REPO=952478859445.dkr.ecr.us-east-1.amazonaws.com/

    $ docker run --rm -it -p8888:8080 ${REPO}mkt-devops/mkt-tenant-splitter:1.0

run the app with your aws credentials mounted as a docker volume

    $ docker run -v ~/.aws:/home/sassrv/.aws --rm -it -p8888:8080 ${REPO}mkt-devops/mkt-tenant-splitter:1.0

set the memory allocation for the container

    $ docker run --memory 500m -v ~/.aws:/home/sassrv/.aws --rm -it -p8888:8080 ${REPO}mkt-devops/mkt-tenant-splitter:1.0
    VM settings:
        Max. Heap Size (Estimated): 483.38M
        Ergonomics Machine Class: server
        Using VM: OpenJDK 64-Bit Server VM

note docker understands how much overhead it should subtract for the container run space + Java heap.

My stuff doesn't work and I'd like to check out the container to see what's going on - let's start the container and jump into it running a bash shell:

    $ docker run -v ~/.aws:/home/sassrv/.aws --rm -it -p8888:8080 ${REPO}mkt-devops/mkt-tenant-splitter:1.0 /bin/bash
    bash-4.4$ ls
    bin      dev      etc      home     install  lib      media    mnt      proc     root     run      sbin     srv      sys      tmp      usr      var
    bash-4.4$ ls -al /home/sassrv
    total 8
    drwxr-sr-x    1 sassrv   sas           4096 Oct 25 20:36 .
    drwxr-xr-x    1 root     root          4096 Jun 21 14:01 ..
    drwxr-xr-x    6 sassrv   sas            192 Oct 25 13:49 .aws
    bash-4.4$ ls /install/citng/service/
    config       service.jar

    bash-4.4$ ps -ef | grep java
    42 sassrv    0:00 grep java

note the last command - my Java application isn't running - I basically told the container to run /bin/bash *instead* of CMD in my Dockerfile.

What if I want both - a normally running application and bash access?

    $ docker run -v ~/.aws:/home/sassrv/.aws --rm -it -p8888:8080 ${REPO}mkt-devops/mkt-tenant-splitter:1.0
    # get the container name
    $ docker ps
    CONTAINER ID        IMAGE                   COMMAND                  CREATED             STATUS                             PORTS                    NAMES
    e49e3b2f6b7f        mkt-devops/mkt-tenant-splitter:1.0   "java -XX:+UnlockExp…"   26 seconds ago      Up 25 seconds (health: starting)   0.0.0.0:8888->8080/tcp   condescending_mayer

    # attach to the container with the bash shell
    $ docker exec -it e49e3b2f6b7f /bin/bash

    # is our application running?
    bash-4.4$ ps -ef | grep java
    1 sassrv    1:11 java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -XshowSettings:vm -Dlogging.config=classpath:logback-spring.xml -jar /install/citng/service/service.jar
    86 sassrv    0:00 grep java

    # can we do normal bash stuff?
    bash-4.4$ ls /home/sassrv/.aws/
    awskeyconfig  config        credentials

    # can we hit our application from inside of the container?
    bash-4.4$ curl http://localhost:8080/tenantSplitter/commons/ping
    OK

All good, we're in our container with our application up and running. Note inside the container we're listening on port 8080 and outside of the container we've exposed this as port 8888.

### Cleaning things up

We've been using the *--rm* flag to run our containers so they get stopped and removed on termination. If we hadn't and wanted to clean up a bunch of containers we can just run:
    $ docker kill $(docker ps -a -q)

If we've been tweaking and build our application we might have a lot of images on our laptop:

    $ docker images | wc
     251    1756   37150

I have 250 images stored locally (wc is also counting the header from the docker images command). I'm going to delete them all - they'll come back as I rebuild things:

    $ docker image rm $(docker images -q)
    $ docker images | wc
     169    1182   25012

    # still a lot - the above command won't delete images that have child images by default. Lets force the issue
    $ docker image rm --force $(docker images -q)
    $ docker images | wc
    1       6      85

All gone!

Let's rebuild our image:

    # we'll add a few more environment variables to simply things
    $ export VERSION=normandy
    $ export PROJECT=mkt-tenant-splitter
    $ docker build --build-arg REPO --build-arg VERSION --build-arg PROJECT -t ${REPO}mkt-devops/mkt-tenant-splitter${VERSION} .

This took longer to build because we had to download our alpine base image again, but subsequent builds will be fast.

Can we make this simpler? Yes, everything we do is included in our gradle build. Run gradlew tasks and you'll see a *Docker tasks* section that has a bunch of docker related commands. A *gradlew build* command will build your docker image, spin up a container and attempt to hit its healthcheck endpoint:

    $ gradlew build

## Kubernetes

### Setup kubectl to hit our cluster

We're going to be using an EKS-based kubernetes cluster named baldur-eks. Most of what we'll do is documented extensively here: https://docs.aws.amazon.com/eks/latest/userguide/what-is-eks.html

You'll need to setup your kubectl kubernetes client to hit the baldur-eks cluster:

    $ aws eks update-kubeconfig --name baldur-eks

Now if we ask kubectl to display our current context it should show the baldur-eks cluster:

    $ kubectl config current-context
    arn:aws:eks:us-east-1:952478859445:cluster/baldur-eks
    # looks good, we're pointed at the baldur-eks cluster

    $ kubectl cluster-info
    Kubernetes master is running at https://DF08F33D88552F7657A214DDEFBCB8EE.gr7.us-east-1.eks.amazonaws.com
    CoreDNS is running at https://DF08F33D88552F7657A214DDEFBCB8EE.gr7.us-east-1.eks.amazonaws.com/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
    Metrics-server is running at https://DF08F33D88552F7657A214DDEFBCB8EE.gr7.us-east-1.eks.amazonaws.com/api/v1/namespaces/kube-system/services/https:metrics-server:/proxy
    # again looks good, our endpoints are the eks control plane

### Now lets start playing with kubernetes
For the following examples I'll largely be using the **ghost** image from: https://hub.docker.com/_/ghost/ - partly because it's nearly halloween (Oct 28, 2019) and it makes a fitting image name, mostly because it has a UI and stateful content that needs attached storage, so we'll be able to test a range of Kubernetes features, and it only takes 4.5 secs to fully boot.

start it up with:

    $ docker run --rm -p2368:2368 ghost

and browse to http://localhost:2368 to see your site, or http://localhost:2368/ghost to create a new one.

Or run it and mount a local drive:

    $ docker run --rm --name baldur-ghost -p 2368:2368 -v /var/lib/ghost/content:/path/to/ghost/blog ghost

You'll have to make /var/lib/ghost read/writable in your OS and add that path in Docker -> Preferences -> File Sharing in your docker installation.

#### Namespaces
We'll use the namespace *ci360* for our applications so lets set this up as our default namespace:

    # set up the namespace
    $ kubectl config set-context --current --namespace=ci360

    # see what's running in it (should be nothing since we haven't deployed anything)
    $ kubectl get pods
    No resources found in ci360 namespace.

To use this we'll have to actually create the namespace:

    $ kubectl create namespace ci360

#### Our first Pod
Now we'll create a Pod, which is a wrapper around one or more docker containers, and is the base building block of all k8s application deployments.

First create a yaml file that describes the pod (baldur-ghost.yaml):

    apiVersion: v1
    kind: Pod
    metadata:
      name: baldur-ghost
    spec:
      containers:
      - name: ghost
        image: ghost

Then we'll use kubectl to deploy it:

    $ kubectl apply -f baldur-ghost.yaml

    $ kubectl get pods
    NAME           READY   STATUS    RESTARTS   AGE
    baldur-ghost   1/1     Running   0          18s

Note kubectl is automatically deploying to the ci360 namespace and reading from the ci360 namespace since we told it to in the previous section.

We can examine the logs:

    $ kubectl logs baldur-ghost

We can login to trouble-shoot the running container:

    $ kubectl exec -it baldur-ghost /bin/bash

To actually access the website running in this Pod we'll have to expose it to the outside world. We'll forward the port the container is running on, 2368, to port 8080 on our local machine (we'll discuss better ways to do this in the following section):

    $ kubectl port-forward baldur-ghost 8080:2368

Now we can hit the site at: http://localhost:8080/

For a detailed view of the Pod's configuration run:

    $ kubectl describe pod baldur-ghost

When we're all done we can delete the Pod with:

    $ kubectl delete -f baldur-ghost.yaml

### Production Applications
With the above we have a Pod running our service in Kubernetes. But it needs a weird port-forward to be accessible external to the cluster, it's a single Pod that will bring the application down on failure, it has no ability to scale, and no health checks. Let's fix that in the following sections.

#### Health Checks
Kubernetes has a built-in liveness probe and a built-in readiness probe.

##### livenessProbe
The livenessProbe is meant to check that the application is running **and** functioning properly. It takes an HTTP endpoint (actually a variety of endpoints - you can invoke a script or do anything you want) and if it doesn't get an HTTP response greater than 200 and less than 400 it will fail and Kubernetes will **restart our application**.

In the following we'll hit a URL that doesn't exist (/ghostdoesntexistlp), and will therefore return an HTTP 400 response. In a real application this should be a health check that returns a 400 when the application hits a state where it's up but can't function properly anymore - corrupted data structures, out of heap space, etc, and only a restart can fix it. This URL should **not** return an HTTP 400 if an external dependency goes down! If, say Redis, goes down and we need it, our application is not going to be fixed by rebooting **our application** and cycling through endless restarts isn't going to help anything!

baldur-ghost.yaml:

    apiVersion: v1
    kind: Pod
    metadata:
      name: baldur-ghost
    spec:
      containers:
      - name: ghost
        image: ghost
        livenessProbe:
            httpGet:
                path: /ghostdoesntexistlp
                port: 2368
            initialDelaySeconds: 5
            timeoutSeconds: 1
            periodSeconds: 10
            failureThreshold: 3

livenessProbe section above sets up the following:
* path, port: kubernetes sends an HTTP GET request to /ghostdoesntexistlp at port 2368
* initialDelaySeconds: it will not be called until 5 seconds have passed from Pod creation
* timeoutSeconds: the probe must respond within 1 sec and must return a status > 200 & < 400
* periodSeconds: kubernetes will call the endpoint every 10 seconds
* failureThreshold: if the check fails 3 times kubernetes will restart the application

Let's take a look at our pods:

    $ kubectl apply -f baldur-ghost.yaml

    $ kubectl get pods
    NAME           READY   STATUS    RESTARTS   AGE
    baldur-ghost   1/1     Running   0          9s

    $ kubectl get pods
    NAME           READY   STATUS    RESTARTS   AGE
    baldur-ghost   1/1     Running   1          37s

    $ kubectl get pods
    NAME           READY   STATUS    RESTARTS   AGE
    baldur-ghost   1/1     Running   2          64s

##### readinessProbe
In the following we'll hit a URL that doesn't exist (/ghostdoesntexistrp), and will therefore return an HTTP 400 response. In a real application this should be a health check that returns a 400 when the application is up and healthy - has free heap space, uncorrupted data structures, and running web server, etc, but **isn't ready to serve requests**. This should fail if external dependencies aren't up. Kubernetes won't restart our application if it fails the readiness Probe, it will just remove the Pod from the service load balancers so it won't receive any traffic.

baldur-ghost.yaml:

    apiVersion: v1
    kind: Pod
    metadata:
      name: baldur-ghost
    spec:
      containers:
      - name: ghost
        image: ghost
        readinessProbe:
            httpGet:
                path: /ghostdoesntexistrp
                port: 2368
            initialDelaySeconds: 5
            timeoutSeconds: 2
            periodSeconds: 10
            failureThreshold: 3

The parameters for the readinessProbe are identical to those for the livenessProbe above.

Again let's take a look at our pods:

    $ kubectl apply -f baldur-ghost.yaml

    $ kubectl get pods
    NAME           READY   STATUS    RESTARTS   AGE
    baldur-ghost   0/1     Running   0          37s

    $ kubectl get pods
    NAME           READY   STATUS    RESTARTS   AGE
    baldur-ghost   0/1     Running   0          110s

Here Kubernetes isn't restarting our application. It's just noting that it isn't ready to receive requests.

##### Health Check Summary
Liveness determines if an application is running properly. Pods that fail liveness checks are restarted. Do not include access to external dependencies in a livenessProbe check.

Readiness describes when a container is ready to serve requests. Pods that fail readiness checks are removed from service load balancers.

Combining readiness and liveness probes ensure only healthy containers are running within the cluster.

#### Resource Management
It would be nice to help Kubernetes pack as many applications as possible on a single machine so that we maximize our aws resource usage. We can do this by telling Kubernetes about the resources our application needs. We can specify:
* requests: the minimum amount of resources required to run the application
* limits: the maximum amount of resources the application can consume

##### requests

The most common types of resource requests are **CPU** and **memory** but Kubernetes has support for others.  

In the following will set up our resource requests:
baldur-ghost.yaml:

    apiVersion: v1
    kind: Pod
    metadata:
      name: baldur-ghost
    spec:
      containers:
      - name: ghost
        image: ghost
        resources:
            requests:
                cpu: "500m"
                memory: "128Mi"

Now if we describe our Pod we'll set it has a cpu and memory request section:

    $ kubectl describe pod baldur-ghost
    ...
    Requests:
      cpu:        500m
      memory:     128Mi
    ...

Note Pods can have more that one container, and resource requests are **per container**. Kubernetes will sum up the container requests when trying to place the pod.

Also note, if we set our request at 0.5 CPUs and our pod is the only one running on a node with 2 CPUs, our pod will use the full 2 CPUs. If we drop another similar pod onto the node they will each use 1 CPU. If we drop 2 more such pods on the node they will drop to 0.5 CPUs each and the node will be considered full - Kubernetes will not deploy any more pods to the node.

Memory requests are handled the same way, but since we can't just grab memory back from a running container, if we are over our requested memory and further pods are scheduled to the node, kubernetes will stop our pod and restart it at a lower memory limit.

##### limits
We can set a maximum on a Pod's resource usage via limits. We can combine this with our requests as per the following:

baldur-ghost.yaml:

    apiVersion: v1
    kind: Pod
    metadata:
      name: baldur-ghost
    spec:
      containers:
      - name: ghost
        image: ghost
        resources:
            requests:
                cpu: "500m"
                memory: "128Mi"
            limits:
                cpu: "1000m"
                memory: "256Mi"

Let's check out our pod again:

    $ kubectl describe pod baldur-ghost
    ...
    Limits:
      cpu:     1
      memory:  256Mi
    Requests:
      cpu:        500m
      memory:     128Mi
    ...

Now kubernetes will not allow our pod to use more than 1 CPU or 256 MB of RAM no matter how much of each is available on the node.

You can find a detailed discussion on how to set resource limits (and what Mi and m mean) here: https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/

#### Persisting Data / Volumes
The containers in our pods are like mini linux machines, they have a file system they can use and manipulate like any other linux box. But it's transitory, when the container is restarted all the data in the container's filesystem is deleted.

##### Setup
For this we're going to use our baldur-tensplit application deployed as a replica set. This deploys Pods like our previous examples did, but deploys them as a replicaset of pods - you can scale in more and scale them back down dynamically.

We'll start with just one replica, and set up the disk writes so that they go to the container's internal disk, which will get erased as the Pods (and hence containers) are scaled in and out.

containerdisk.yaml

    apiVersion: extensions/v1beta1
    kind: ReplicaSet
    metadata:
      name: baldur-tensplit
    spec:
      replicas: 1
      template:
        metadata:
          labels:
            app: baldur-app
            version: "1"
        spec:
          containers:
            - name: baldur-cname
              image: 952478859445.dkr.ecr.us-east-1.amazonaws.com/mkt-devops/mkt-tenant-splitter:baldur
              imagePullPolicy: Always

Let's run this:

    $ kubectl apply -f containerdisk.yaml
    replicaset.extensions/baldur-tensplit created
    $ kubectl get pods
    NAME                    READY   STATUS    RESTARTS   AGE
    baldur-tensplit-rzvbr   1/1     Running   0          21s
    $ kubectl get replicasets
    NAME              DESIRED   CURRENT   READY   AGE
    baldur-tensplit   1         1         1       7m17s

    $ kubectl port-forward baldur-tensplit-rzvbr 8888:8080

Note above the replicaset gets our baldur-tensplit name and the pod gets a random string tacked on (-rzvbr).

If we hit the Get Quotes button we'll get our 3 canned quotes:

    It was the best of times it was the worst of times.
    Life is never fair, and perhaps it is a good thing for most of us that it is not.
    People say nothing is impossible but I do nothing all day.

We'll add two quotes (hello & goodbye) and hit the get quotes button:

    It was the best of times it was the worst of times.
    Life is never fair, and perhaps it is a good thing for most of us that it is not.
    People say nothing is impossible but I do nothing all day.
    hello
    goodbye

Let's scale the pod out then back in:

    $ kubectl scale replicasets baldur-tensplit --replicas=0
    $ kubectl scale replicasets baldur-tensplit --replicas=1

Then grab the new pod name and port-forward to it:

    $ kubectl get pods
    NAME                    READY   STATUS    RESTARTS   AGE
    baldur-tensplit-469g4   1/1     Running   0          8s
    $ kubectl port-forward baldur-tensplit-469g4 8888:8080

If we hit the Get Quotes button we'll just get our 3 canned quotes again - new Pod/container gives us a new internal filesystem:

    It was the best of times it was the worst of times.
    Life is never fair, and perhaps it is a good thing for most of us that it is not.
    People say nothing is impossible but I do nothing all day.

##### Mounting a drive on our node
