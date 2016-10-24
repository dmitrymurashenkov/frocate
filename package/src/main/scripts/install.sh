#!/bin/bash

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

#This installs ./frocate.tar.gz and dependencies

#Create user
adduser --disabled-login -gecos "" frocate

#Fix perl warning about locale
locale-gen en_GB.UTF-8 en_US.UTF-8

#Install java
apt-get install -y software-properties-common
add-apt-repository ppa:webupd8team/java -y
apt-get update
echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
apt-get install -y oracle-java8-installer

#Install docker and build image (requires Dockerfile from task-runner project)
apt-get install -y docker.io
sudo usermod -aG docker frocate

#Requires Dockerfile-executable-vm and Dockerfile-tests-vm files in current folder
docker build --pull -t frocate-executable-vm:1 -f Dockerfile-executable-vm .
#Gid of "docker" group in VM must match one on the host
docker build --pull --build-arg DOCKER_GID=`getent group docker | cut -d: -f3`  -t frocate-tests-vm:1 -f Dockerfile-tests-vm .

#Remove old files
rm -rf /opt/frocate/*

#Unpack
tar -xf frocate.tar.gz -C /

#Make root own everything
chown -R root:root /opt/frocate
chmod 755 /etc/init.d/frocate
ln -sT /opt/frocate/etc/ /etc/frocate

#Jetty is run from 'frocate' user
mkdir -p /var/log/frocate
chown -R frocate:frocate /var/log/frocate

mkdir -p /var/opt/frocate/result
chown -R frocate:frocate /var/opt/frocate

#Run jetty upon startup
update-rc.d frocate defaults