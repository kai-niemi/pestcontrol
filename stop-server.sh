#!/bin/bash

pid=$(ps -ef | grep "java" | grep "battery" | awk '{print $2}')

if [ -x ${pid} ]; then
   echo -e "No pestcontrol.jar process found - is it running?"
   exit 1
fi

kill -TERM $pid
RETVAL=$?

echo -e "Stopped service (pid: $pid) $RETVAL"