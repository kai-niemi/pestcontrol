#!/bin/bash

pid=$(ps -ef | grep "java" | grep "pc.jar" | awk '{print $2}')

if [ -x ${pid} ]; then
   fn_print_error "No pc.jar process found - is it running?"
   exit 1
fi

kill -TERM $pid
RETVAL=$?

fn_print_ok "Stopped service (pid: $pid) $RETVAL"