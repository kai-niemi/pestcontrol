#!/bin/bash

## Executable jar
APP_JARFILE=${rootdir}/pestcontrol.jar

if [ ! -f "$APP_JARFILE" ]; then
    fn_fail_check ./mvnw clean install
    pomVersion=$(echo 'VERSION=${project.version}' | ./mvnw help:evaluate | grep '^VERSION=' | sed 's/^VERSION=//g')
    ln -sf target/pestcontrol.jar ${APP_JARFILE}
fi

java -jar ${APP_JARFILE} $*
