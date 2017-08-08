#!/bin/bash

for f in `ls testsWithoutFootprint/test* |sort`;do
  TESTNAME=`echo $f |sed -e 's/.*\///g' -e 's/\.sh$//g'`
  printf 'Running test: %-60s: ' $TESTNAME
  bash $f
  rc=$?
  if [ ${rc} == 0 ];then
    printf "SUCCESS\n"
  else
    printf "FAILED\n"
  fi
  if [ ${rc} == 2 ];then
    echo "Wrong password provided. Stopping test execution."
    echo "Otherwise user ${CM_USER} will be locked."
    break
  fi
done
