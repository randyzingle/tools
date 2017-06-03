Base Java Code
==============

initialize gradle project

first update to latest version of gradle
sdk upgrade gradle 3.3

initial project with wrapper
gradle init --type java-library

create folders for subproject
base
lambda
network

edit settings.gradle to add include(s)


add the following to build.gradle
apply plugin: 'eclipse'

copy build.gradle file to each subproject

for top level project change 
apply plugin: 'java' to
apply plugin: 'base'

create the src folders for the subprojects
src/main/java
src/test/java


run gradle eclipse
you'll get the .project files for the base folder and subprojects
you'll get the .classpath files for each subproject with the src/* folders in classpath

Two important objects available in a gradle build:
project -> system being built
task -> encapsulates a piece of build logic

for multi-project builds a Project instance is created for the root project and for each child project (for each build.gradle)

methods in the build without object references are invoked on the project object

the task objects are created for each task declared in the build file & in plugins

note some tasks are available in all projects like
help
tasks

gradle lifecycle:
- initialization
- configuration
- execution

gradle lifecycle callbacks
for example, there is an evaluation phase run during Configuration, and Project has the following hooks:
- beforeEvaluate
- afterEvaluate

you can define 'extra' properties through the ext namespace
project.ext.prop1 = "foo"

project.version (or just version) will be used by tasks such as Jar to include a version number in the produced artifact

gradle ships with many custom tasks types built in
Copy, Exec, Delete, Jar, Synch, Test, JavaCompile, Zip, ...

gradle built in task: uploadArchive, customized with archives configuration

you can call subproject tasks directly two ways:

gradle base:list
gradle -p base list

check out dependencies:
gradlew -q dependencies base:dependencies

list project properties
gradlew -q base:properties

run build without test:
gradlew build -x test

list all projects:
gradlew projects

list all tasks:
gradlew base:tasks (will only show tasks belonging to a group)
tasks --all will show all tasks

show details for a dependency:
gradlew base:dependencyInsight --configuration compile --dependency guava

show available properties in a project:
gradlew -q base:properties

note one of the properties is the dependency cache:
dependencyCacheDir: /home/rzingle/work/projects/tools/java/base/build/dependency-cache

Terminology
dependencies -things that are needed BY our project
these are grouped into configurations
The java plugin defines a number of standard configurations
- compile
- runtime
- testCompile
- testRuntime
