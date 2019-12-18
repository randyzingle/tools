# Docker and ECS

### Creating your project from the mkt-docker-template project

#### Initial Setup

Setup a new project in your gitlab island and copy all of the files in mkt-template-docker (except the .git directory) to it

In this example well use **mkt-myisland** as the island name (no that's not your actual island name, don't use it), and **mkt-tenant-splitter** as the project name (see previous comment).

change all reference from mkt-myisland to mkt-myisland and all references to mkt-docker-template to mkt-tenant-spitter, you'll have to update the following files:
* build.gradle files
* cloudformation.json
* environments.yaml
* gradle.properties
* settings.gradle

change the cloudGroup name in gradle.properties to cloudGroup=MyIsland

change the Mappings/Constants/InstanceValues/ECRImageName to mkt-myisland/mkt-tenant-splitter

edit the Dockerfile and update the maintainer name and the context-root in the HEATHCHECK CMD

    ARG REPO
    ARG RELEASE=latest
    FROM ${REPO}mkt-docker/alpine-jdk8-min:${RELEASE}
    ARG RELEASE=unknown
    ARG VERSION=unknown
    ARG PROJECT=unknown
    MAINTAINER cibuild <cibuild@sas.com>
    LABEL name=${PROJECT}                           \
      description="CI360 Template Microservice" \
      vendor="SAS Institute Inc."               \
      version=${VERSION}                        \
      revision=${REVISION}                      \
      maintainer="randall.zingle@sas.com"

    USER sassrv

    COPY service/ /install/citng/service/

    EXPOSE 8080

    HEALTHCHECK CMD /usr/bin/curl --fail http://localhost:8080/tenantSplitter/commons/ping || exit 1

    CMD ["java", \
    "-XX:+UnlockExperimentalVMOptions",  \
    "-XX:+UseCGroupMemoryLimitForHeap", \
    "-XX:MaxRAMFraction=1", \
    "-XshowSettings:vm", \
    "-Dlogging.config=classpath:logback-spring.xml", \
    "-jar", \
    "/install/citng/service/service.jar"]

log in to our Amazon Docker Registry (ECR)

    $ aws ecr get-login --no-include-email --region us-east-1 | /bin/bash

create the initial repository for your image:

    $ gradlew ecrInit

this will create an ECR repository named <island-name/project-name> which in this case will be mkt-myisland/mkt-tenant-splitter, check it out in the aws console

#### Build the Application and create a docker image

build an image from the Dockerfile

just run:

    $ gradlew build

if you've built docker images before, and want to map that knowledge to what the gradlew build is doing, you can do it manually

    $ cd mkt-tenant-splitter-service/build/docker
    $ export PROJECT=mkt-tenant-splitter
    $ export VERSION=2001
    $ export REPO=952478859445.dkr.ecr.us-east-1.amazonaws.com/
    docker build \
    --build-arg REPO --build-arg VERSION --build-arg PROJECT \
    -t rzingle/baldur-ts .

note you can specify --build-arg directly:
* --build-arg VERSION=2001

or you can set an environment variable and just specify the variable name:
* --build-arg VERSION (with VERSION set as an environment variable in the local shell as shown above)

check it out, you have a local docker image:

    $ docker images | grep myisland
    mkt-myisland/mkt-tenant-splitter latest e4cef8f9fabb 4 minutes ago 149MB                                           

You're curious and are wondering, has it been pushed to the ECR Repo yet?

    $ aws ecr list-images --repository-name mkt-myisland/mkt-tenant-splitter
    {
    "imageIds": []
    }

No it hasn't - because we are using stackPrefix=null. Once we are deploying to a cloudformation stack and ECS it will populate the ECR registry.

#### Run your application locally
Try running your image locally

    $ docker run --rm -it -p8888:8080 mkt-myisland/mkt-tenant-splitter

* **--rm** will kill the container when you hit CTRL-c
* **-it** runs the container in interactive mode -> you can see the console output
* **-p:8888:8080** maps the container's port 8080 to port 8888 on your machine
* **rzingle/baldur-ts** is your local image (reponame/imagename)

Browse to a local endpoint for your service: http://locahost:8888/tenantSplitter/ui/config.html

or if you love the command line:

    $ curl http://localhost:8888/tenantSplitter/commons/ping
    OK

So now you can run your application locally either directly from your IDE or by building the docker container and running it.

Now lets try hitting the tenant splitter endpoint that lists our aws S3 buckets from our app running in IntelliJ:

    $ curl http://localhost:8080/tenantSplitter/s3/bucketlist
    [angularspa.ci.tc, aws-athena-query-results-952478859445-us-east-1, aws-athena-query-results-952478859445-us-west-2, aws-codestar-us-east-1-952478859445, ....

Lets do the same with our docker container:

    $ docker run --rm -it -p8888:8080 mkt-myisland/mkt-tenant-splitter
    $ curl http://localhost:8888/tenantSplitter/s3/bucketlist
    {"errorCode":0,"details":["correlator: 0f04f683-fafa-4694-a62e-bd78e0f456fd","traceId: b127c96eb2332214", ....

It doesn't work. Our docker container can't see our laptop's file system so it can't read our aws credentials in **~/.aws/credentials**. This is ok when it's running in ECS or EKS because the infrastructure will inject credentials for us. We can do this locally by mounting the credentials as a docker volume when we start the container.

    $ docker run --rm -it -p8888:8080 -v ~/.aws:/home/sassrv/.aws  mkt-myisland/mkt-tenant-splitter
    $ curl http://localhost:8888/tenantSplitter/s3/bucketlist
    [angularspa.ci.tc, aws-athena-query-results-952478859445-us-east-1, aws-athena-query-results-952478859445-us-west-2,

Now it works! The flag **-v ~/.aws:/home/sassrv/.aws** mounts our laptop's home folder/.aws folder under a folder in the container named /home/sassrv/.aws. We're running our application as user sassrv so the aws sdk looks here when running through the credential lookup chain.

#### Run your application in ECS
Now we can do a full build and deploy to ECS and our application will run as a docker container on one of our ECS environments. You can either kick this off as a Jenkins job or via gradle with something like this:

    $ gradlew all -Dstack=baldur -DbuildType=personal -Downer=razing -DadminEmail=randall.zingle@sas.com -x test -x integrationTest

Your cloudformation.json file has a parameter named ECSStackName which tells you which ECS environment your container will be deployed to.
