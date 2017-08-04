#!/bin/bash

if [ -z ${CM_USER} ];then
  export CM_USER="john.doe"
fi

if [ -z ${CM_PASSWORD} ];then
  echo "[ERROR] Provide password for user '${CM_USER}' with environment variable '\${CM_PASSWORD}'."; exit 1
fi

if [ -z "${CM_ENDPOINT}" ];then
  export CM_ENDPOINT="https://example.org/endpoint/"
fi


MAVEN_BUILD_DIR="../../../target"

if [ ! -d "${MAVEN_BUILD_DIR}" ];then
  echo "[ERROR] Maven build directory '${MAVEN_BUILD_DIR}' does not exist. Run a maven build ..."; exit 1
fi

CM_CLIENT_HOME="${MAVEN_BUILD_DIR}/cmclient"

TAR_FILE_NAME=`ls ${MAVEN_BUILD_DIR} |grep dist\.cli.*\.tar.gz`
TAR_FILE="${MAVEN_BUILD_DIR}/${TAR_FILE_NAME}"

if [[ -z "${TAR_FILE_NAME}"  ||! -f "${TAR_FILE}" ]];then
  echo "[ERROR] Tar file '${TAR_FILE}' not found.";exit 1
fi

if [ -e ${CM_CIENT_HOMT} ];then
  rm -rf ${CM_CLIENT_HOME} |exit 1
fi

mkdir ${CM_CLIENT_HOME} |exit 1

tar -C ${CM_CLIENT_HOME} -xf "${TAR_FILE}" |exit 1


