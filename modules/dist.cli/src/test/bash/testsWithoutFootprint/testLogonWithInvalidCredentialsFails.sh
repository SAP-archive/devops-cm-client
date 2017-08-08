#!/bin/bash

. ./prepare.sh

EXPECTED_RC=2

STDERR_OUTPUT=`${CM_CLIENT_HOME}/bin/cmclient \
  -e ${CM_ENDPOINT} \
  -u DOES_NOT_EXIST \
  -p WRONG \
  is-change-in-development 8000038673 2>&1 1>/dev/null`

rc=$?

if [ ${rc} != ${EXPECTED_RC} ];then
  echo "Invalid return code received: '${rc}'. Should be '${EXPECTED_RC}'."
  exit 1
fi

echo $STDERR_OUTPUT |grep "401" > /dev/null
