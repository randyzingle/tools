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


