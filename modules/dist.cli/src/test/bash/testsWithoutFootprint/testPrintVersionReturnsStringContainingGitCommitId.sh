#!/bin/bash

. ./prepare.sh

VERSION=`${CM_CLIENT_HOME}/bin/cmclient --version`

rc=$?

if [ ${rc} != 0 ];then
  exit ${rc}
fi

echo $VERSION |grep -e "^.* : [0-9,a-f]\{40\}$" > /dev/null

