#!/bin/bash

## Executable jar
APP_JARFILE=${rootdir}/pestcontrol.jar

if [ ! -f "$APP_JARFILE" ]; then
    APP_JARFILE=target/pestcontrol.jar
    if [ ! -f "$APP_JARFILE" ]; then
        fn_fail_check ./mvnw clean install
    fi
fi

java -jar ${APP_JARFILE} $*
