#!/bin/bash

# We need to skip the tests in the container because of the container build
# uses /deployments/startCaptureScript.sh for the capture script, and that's
# outside of the project folder, which is unsuitable for tests.

./mvnw clean compile package -Pcontainer
DOCKER_BUILDKIT=1 docker build -f target/docker/Dockerfile.alpine -t git.rtdstech.com:4567/jdh/pcapture .
docker push git.rtdstech.com:4567/jdh/pcapture
