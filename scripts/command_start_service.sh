#!/bin/bash

## Executable jar
APP_JARFILE=${rootdir}/pestcontrol.jar

if [ ! -f "$APP_JARFILE" ]; then
    APP_JARFILE=target/pestcontrol.jar
    if [ ! -f "$APP_JARFILE" ]; then
        fn_fail_check ./mvnw clean install
    fi
fi

pid=$(ps -ef | grep "java" | grep "pestcontrol.jar" | awk '{print $2}')

if [ ! -x ${pid} ]; then
   fn_print_error "Existing process ${pid} found - is it running?"
   exit 1
fi

mkdir -p ${logdir}

nohup java -jar ${APP_JARFILE} ${app_params} $* > ${logdir}/pestcontrol-stdout.log 2>&1 &

fn_print_ok "Started service - check '${logdir}/pestcontrol-stdout.log' for status"