#!/usr/bin/env bash

IMAGE_TAG=$DOCKER_USERNAME/spark-$1:latest
docker tag spark:latest $IMAGE_TAG

docker push $IMAGE_TAG

# trigger autoredeploy for the affected environment