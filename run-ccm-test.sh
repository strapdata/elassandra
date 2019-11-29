#!/bin/bash

echo "GO TO INTEGRATION TEST DIRECTORY"
cd integ-test

echo "RUN INTEGRATION TEST"
TARBALL=$(ls ../distribution/tar/build/distributions/elassandra*.gz)
./test-cleanup-repair.sh $TARBALL
