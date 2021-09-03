#!/bin/bash

docker run --rm --net=host --privileged --mount source=pcapture,target=/data -e ETHERNET_INTERFACE_NAME=enp0s31f6 pcapture:latest
