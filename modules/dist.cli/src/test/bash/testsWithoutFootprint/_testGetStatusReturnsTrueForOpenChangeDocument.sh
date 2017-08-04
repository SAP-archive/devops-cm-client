#!/bin/bash

. ./prepare.sh

# TODO: switch back when john.doe is unlocked.

IS_IN_DEVELOPMENT="true"



#IS_IN_DEVELOPMENT=`${CM_CLIENT_HOME}/bin/cmclient \
#  -e ${CM_ENDPOINT} \
#  -u ${CM_USER} \
#  -p ${CM_PASSWORD} \
#  is-change-in-development 8000038673`

test "${IS_IN_DEVELOPMENT}" = "true"
