#!/bin/bash

# docker run --rm --mount source=pcapture,target=/data busybox /bin/sh -c 'chown 1001:1001 /data && chmod 550 /data'
docker run --rm --net=host --privileged --mount source=pcapture,target=/data git.rtdstech.com:4567/jdh/pcapture:latest
