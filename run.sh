#!/bin/sh
#
# build zip package, but ensuring its from the current source
# turn off tests and other validation to speed it up
# TODO: can be sped up more, if shading is moved out of core/
ulimit -u 65535
mvn -f dev-tools/pom.xml install
mvn -f rest-api-spec/pom.xml install
mvn -f javassist-maven/pom.xml install

mvn -am -pl dev-tools,distribution/zip package -DskipTests -Drun -Pdev
