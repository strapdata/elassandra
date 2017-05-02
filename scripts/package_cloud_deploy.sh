#!/usr/bin/env bash

GREEN='\033[1;32m'
RED='\033[1;31m'
MAGENTA='\033[1;35m'
NOCOLOR='\033[0m'
CYAN='\033[1;36m'
NOCOLOR='\033[0m'

echo_color() {
    color="$1"
    shift
    text="$@"
    echo -e "[${color}$text${NOCOLOR}]";
}

show_and_exec(){
  echo_color $CYAN "$@"
  eval "$@"
}

show_and_exec package_cloud push elassandra/latest/debian/jessie $DEBPKG
show_and_exec package_cloud push elassandra/latest/el/7 $RPMPKG
