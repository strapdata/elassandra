#!/usr/bin/env bash

#
# This script bumps the version of elassandra in the gke marketplace repository
#

# fails on error and trace execution
set -ex

BRANCH=master
GITHUB_REPO=strapdata/elassandra-google-k8s-marketplace.git

init() {
  # shallow clone of the elassandra docker repository
  rm -rf gke-marketplace
  git clone --depth 1 --single-branch --branch ${BRANCH} https://github.com/${GITHUB_REPO} gke-marketplace
  cd gke-marketplace

  if [ "$TRAVIS" = "true" ]; then

    # GITHUB_PUSH_TOKEN=... set from travis

    git remote set-url origin https://${GITHUB_PUSH_TOKEN}@github.com/${GITHUB_REPO}
    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "Travis CI"
    NEW_VERSION=$(echo ${TRAVIS_TAG} |  sed 's/v\([0-9]*\.[0-9]*.\.[0-9]*\.[0-9]*\).*/\1/')

    if [[ ${TRAVIS_TAG} != v6.2.3.+([0-9]) ]]; then
      echo "gke-marketplace.sh should only run with release tags v6.2.3.x"
      return 1
    fi

  else
    NEW_VERSION=$1
  fi

  if [[ ${NEW_VERSION} == 6.2.3.* ]]; then
    return 0
  else
    echo "gke-marketplace.sh should only run for v6.2.3.x"
    return 1
  fi
}

cleanup() {
  # clean up
  cd ../
  rm -rf gke-marketplace
}

bump_version() {
  sed -i.bak s/'TAG ?= .*'/'TAG ?= '$1/g Makefile
}

commit_push() {
  git add Makefile
  if ! git diff --cached --exit-code; then
    echo "bumping version"
    git commit -m "bump version ${NEW_VERSION}"
    git tag ${NEW_VERSION}
    git push origin ${BRANCH} ${NEW_VERSION}
  else
    echo "the version haven't changed"
  fi
}

main() {
  if init $@; then
    bump_version ${NEW_VERSION}
    commit_push
    cleanup
  else
    cleanup
    return 1
  fi
}

main $@
