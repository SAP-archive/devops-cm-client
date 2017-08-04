#!/bin/bash

. ./prepare.sh

VERSION=`${CM_CLIENT_HOME}/bin/cmclient --version`

echo $VERSION |grep -e "^.* : [0-9,a-f]\{40\}$" > /dev/null

