#!/bin/bash

# We need to skip the tests in the container because of the container build
# uses /deployments/startCaptureScript.sh for the capture script, and that's
# outside of the project folder, which is unsuitable for tests.

./mvnw clean compile package -Pcontainer
docker build -f src/main/docker/Dockerfile.alpine -t quarkus/dumpcap-ws-jvm .
