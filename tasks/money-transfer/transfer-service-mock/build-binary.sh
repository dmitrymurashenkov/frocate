#!/bin/bash

#Packs jar into self-extracting .sh script that can be executed to simulate binary executable

export JAR_NAME=transfer-service-mock-1.0-SNAPSHOT.jar
mvn clean install
mkdir target/build-binary
cp "target/$JAR_NAME" target/build-binary/
src/main/scripts/makeself.sh target/build-binary/ target/executable.sh "My executable" java -Xmx256m -Xms256m -jar "$JAR_NAME" $1