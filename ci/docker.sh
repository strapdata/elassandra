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

  if [ -n "$TRAVIS_TAG" ]; then
    # this function is only called when there is a tag

    export REPO_NAME=${TRAVIS_REPO_SLUG}

    export REPO_DIR=${TRAVIS_BUILD_DIR}
    export ELASSANDRA_COMMIT=${TRAVIS_COMMIT}
    export DOCKER_RUN_TESTS=true

    # extract the elasticsearch version
    ELASTICSEARCH_VERSION=$(echo "$TRAVIS_TAG" | sed 's/v\([0-9]*\.[0-9]*.\.[0-9]*\)\.[0-9]*.*/\1/')

    # publish to docker hub when a tag is set
    export DOCKER_PUBLISH=true

    # add extra tags when on master branches
    if [[ ${TRAVIS_TAG} == v+([0-9]).+([0-9]).+([0-9]).+([0-9]) ]]; then
      export DOCKER_MAJOR_LATEST=true
      # try to infer if the current build need to be tagged "latest". LATEST_VERSION is defined in .travis.yaml
      if [ "$ELASTICSEARCH_VERSION" = "$LATEST_VERSION" ]; then
        export DOCKER_LATEST=true
      fi
    fi

    # call the docker build+test+push script
    build_with_retry

    # publish to gcloud registry. DISABLED, the image is build from https://github.com/strapdata/elassandra-google-k8s-marketplace
    #  gcloud_install
    #  gcloud_auth || return 1
    #  DOCKER_REGISTRY=gcr.io/ REPO_NAME=${GCLOUD_REPO_NAME:-strapdata-factory/elassandra} BASE_IMAGE=launcher.gcr.io/google/debian9:latest build_with_retry

    # If tag = v6.2.3.x, construct the intermediate image for gke
    if [[ ${TRAVIS_TAG} == v6.2.3.+([0-9]) ]]; then
      # build a docker image based on google debian 9, used later as a base image in the elassandra gke marketplace
      REPO_NAME=${REPO_NAME}-debian-gcr BASE_IMAGE=launcher.gcr.io/google/debian9:latest build_with_retry
    fi
  fi
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