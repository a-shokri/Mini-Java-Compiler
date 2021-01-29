#!/usr/bin/env bash

clear

option=$1
filename=$2

jar_name=././dist/lib/compiler.jar

if [[ -n ${option} && -n ${filename} ]]
then
    exec java -jar ${jar_name} ${option} ${filename}
else
    echo 'Usage: emjc <options> filename'
    exit 1
fi

exit 1
