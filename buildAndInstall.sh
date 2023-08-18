#!/bin/bash

#
# Builds pg-cdc and installs it as systemctl service
#
# Author : Dinesh Sawant
#
# Usage  : sudo ./buildAndInstall.sh <env>
#

export GRADLE_OPTS=${GRADLE_OPTS}" -Dorg.gradle.daemon=false"
./gradlew clean fatJar

USR_GROUP=appmgr
USR=jvmapps
WORKING_DIR=/opt/services/pg-cdc
ENV=$1

echo "Creating group ${USR_GROUP}"
sudo groupadd -r $USR_GROUP -f

echo "Adding ${USR} to ${USR_GROUP}"
sudo useradd -r -s /bin/false -g $USR_GROUP $USR

echo "Details of user ${USR}"
id $USR

echo "Copying service file"
sudo cp pg-cdc.service /etc/systemd/system/pg-cdc.service

echo "Creating /opt/services"
sudo mkdir -p /opt/services

echo "Creating ${WORKING_DIR}"
sudo mkdir -p $WORKING_DIR

echo "Copying jar to ${WORKING_DIR}"
sudo cp build/libs/pg-cdc-*.jar ${WORKING_DIR}/

CONFIG_PATH=conf/${ENV}/config.properties
echo "Copying ${CONFIG_PATH} to ${WORKING_DIR}"
sudo cp "${CONFIG_PATH}" ${WORKING_DIR}/

echo "Changing owner to ${USR}:${USR_GROUP} for /opt/services"
sudo chown -R $USR:$USR_GROUP /opt/services

sudo systemctl daemon-reload
