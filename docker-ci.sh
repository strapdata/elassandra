#!/usr/bin/env bash

# fails on error and trace execution
set -ex

# shallow clone of the elassandra docker repository
rm -rf docker
git clone --depth 1 https://github.com/strapdata/docker-elassandra.git docker
cd docker

# set parameters
if [ "$TRAVIS" = "true" ]; then
  # Special branching to be ran under travis

  export REPO_NAME=${TRAVIS_REPO_SLUG}
  export REPO_DIR=${TRAVIS_BUILD_DIR}

  if [ -n "$TRAVIS_TAG" ]; then
    # publish to docker hub when a tag is set
    export DOCKER_PUBLISH=true

    # try to infer if the current build need to be tagged "latest"
    ELASTICSEARCH_VERSION=$(echo "$TRAVIS_TAG" | sed 's/v\(.*\..*.\..*\)\..*/\1/')
    if [ "$ELASTICSEARCH_VERSION" = "$LATEST_VERSION" ]; then
      export DOCKER_LATEST=true
    fi
  fi

else
  export REPO_DIR=../
fi

# try 5 times, because gpg servers suck
n=0
until [ $n -ge 5 ]
do
  echo "build try number $n"
  # build and publish the docker image
  ./build.sh && break
  n=$[$n+1]
  sleep 1
done
if [ $n -eq 5 ]; then
  echo "failed to build image"
  exit 1
fi


# clean up
cd ../
rm -rf docker