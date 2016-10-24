#/bin/bash

docker build --pull -t frocate-executable-vm:1 -f Dockerfile-executable-vm .

#Gid of "docker" group in VM must match one on the host
docker build --pull --build-arg DOCKER_GID=`getent group docker | cut -d: -f3`  -t frocate-tests-vm:1 -f Dockerfile-tests-vm .
