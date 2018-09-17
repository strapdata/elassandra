#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
set -ex

# work in the project dir.
cd $(dirname "$0")/..

# If set, the images will be published to docker hub
DOCKER_PUBLISH=${DOCKER_PUBLISH:-true}

# Unless specified with a trailing slash, publish in the public strapdata docker hub
DOCKER_REGISTRY=${DOCKER_REGISTRY:-""}

# If set, the images will be tagged latest
LATEST=${LATEST:-false}

REPO=${TRAVIS_REPO_SLUG:="strapdata/elassandra"}

# Options to add to docker build command
DOCKER_BUILD_OPTS=${DOCKER_BUILD_OPTS:-"--rm"}

# the target names of the images
DOCKER_IMAGE=${DOCKER_REGISTRY}${REPO}

cp ${TRAVIS_BUILD_DIR:-"."}/distribution/tar/build/distributions/elassandra-*.tar.gz docker/
elassandra_tarball=$(ls docker/elassandra-*.tar.gz)
elassandra_version=$(echo $elassandra_tarball | sed 's/.*elassandra\-\(.*\).tar.gz/\1/')

echo "Building docker image for elassandra_tarball=$elassandra_tarball"
docker build --build-arg elassandra_version=$elassandra_version \
             --build-arg elassandra_url=elassandra-${elassandra_version}.tar.gz \
             $DOCKER_BUILD_OPTS -f docker/Dockerfile -t "$DOCKER_IMAGE:$elassandra_version" docker

# push to docker hub if DOCKER_PUBLISH variable is true (replace remote_repository if you want to use this feature)
if [ "$DOCKER_PUBLISH" = "true" ]; then
   docker push $DOCKER_IMAGE:$elassandra_version

   if [ "$LATEST" = "true" ] || [ "$TRAVIS_BRANCH" = "master" ]; then
      echo "Publishing the latest = $elassandra_version"
      docker tag $DOCKER_IMAGE:$elassandra_version $DOCKER_IMAGE:latest
      docker push $DOCKER_IMAGE:latest
   fi
fi

