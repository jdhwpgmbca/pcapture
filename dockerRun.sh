#!/bin/bash

docker run --rm --net=host --privileged --mount source=pcapture,target=/data pcapture:latest
