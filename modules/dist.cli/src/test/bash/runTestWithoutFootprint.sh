#!/bin/bash

for f in `ls testsWithoutFootprint/test* |sort`;do
  TESTNAME=`echo $f |sed -e 's/.*\///g' -e 's/\.sh$//g'`
  printf 'Running test: %-60s: ' $TESTNAME
  bash $f
  if [ $? == 0 ];then
    printf "SUCCESS\n"
  else
    printf "FAILED\n"
  fi
done
