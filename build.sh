#!/bin/zsh

./gradlew dockerBuildImage

cd example

docker-compose up