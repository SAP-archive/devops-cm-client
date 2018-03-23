#!/bin/bash

. ./prepare.sh

IS_IN_DEVELOPMENT=`${CM_CLIENT_HOME}/bin/cmclient \
  -e ${CM_ENDPOINT} \
  -u ${CM_USER} \
  -p ${CM_PASSWORD} \
  is-change-in-development -cID 8000038673`

rc=$?
if [ ${rc} == 2 ];then
  exit ${rc}
fi

test "${IS_IN_DEVELOPMENT}" = "true"
