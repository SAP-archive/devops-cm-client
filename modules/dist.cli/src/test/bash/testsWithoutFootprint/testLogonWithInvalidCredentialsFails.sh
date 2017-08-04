#!/bin/bash

. ./prepare.sh

STDERR_OUTPUT=`${CM_CLIENT_HOME}/bin/cmclient \
  -e ${CM_ENDPOINT} \
  -u DOES_NOT_EXIST \
  -p WRONG \
  is-change-in-development 8000038673 2>&1 1>/dev/null`

echo $STDERR_OUTPUT |grep "401" > /dev/null
