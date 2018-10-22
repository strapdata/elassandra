#!/usr/bin/env bash

# fails on error and trace execution
set -ex

init() {
  # shallow clone of the elassandra docker repository
  rm -rf docker
  git clone --depth 1 https://github.com/strapdata/docker-elassandra.git docker
  cd docker
}

build_with_retry() {
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
}

gcloud_install() {
  # If the SDK is not already cached, download it and unpack it
  if [ ! -d ${HOME}/google-cloud-sdk ]; then
    curl https://sdk.cloud.google.com | bash;
    gcloud -v
  fi
}

gcloud_auth() {
  if [ -z "$GCLOUD_SECRET_KEY" ]; then
    echo "GCLOUD_SECRET_KEY is not set. Can't authenticate with gcloud"
    return 1
  else
    echo "$GCLOUD_SECRET_KEY" | base64 -d > gcloud-secret.json
    gcloud auth activate-service-account --key-file gcloud-secret.json

    # does not work for docker 17.x
    # gcloud beta auth configure-docker
    cat gcloud-secret.json | docker login -u _json_key --password-stdin https://gcr.io
  fi
}

under_travis() {
  # Special branching to be ran under travis

  export REPO_NAME=${TRAVIS_REPO_SLUG}
  export REPO_DIR=${TRAVIS_BUILD_DIR}
  export ELASSANDRA_COMMIT=${TRAVIS_COMMIT}
  export DOCKER_MAJOR_LATEST=true
  export DOCKER_RUN_TESTS=true

  if [ -n "$TRAVIS_TAG" ]; then
    # publish to docker hub when a tag is set
    export DOCKER_PUBLISH=true

    # try to infer if the current build need to be tagged "latest"
    ELASTICSEARCH_VERSION=$(echo "$TRAVIS_TAG" | sed 's/v\(.*\..*.\..*\)\..*/\1/')
    if [ "$ELASTICSEARCH_VERSION" = "$LATEST_VERSION" ]; then
      export DOCKER_LATEST=true
    fi
  fi

  # publish to docker hub
  build_with_retry


  # publish to gcloud registry
  gcloud_install
  gcloud_auth || return 1
  DOCKER_REGISTRY=gcr.io/ REPO_NAME=${GCLOUD_REPO_NAME:-strapdata-factory/elassandra} BASE_IMAGE=launcher.gcr.io/google/debian9:latest build_with_retry
}

manual_run() {
  export REPO_DIR=../
  build_with_retry
}

cleanup() {
  # clean up
  cd ../
  rm -rf docker
}

main() {
  init

  if [ "$TRAVIS" = "true" ]; then
    under_travis
  else
    manual_run
  fi

  cleanup
}

main