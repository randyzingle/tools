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

### Gradle Build

Can we make this simpler? Yes, everything we do is included in our gradle build. Run gradlew tasks and you'll see a *Docker tasks* section that has a bunch of docker related commands. A *gradlew build* command will build your docker image, spin up a container and attempt to hit its healthcheck endpoint:

    $ gradlew build
