#!/bin/bash

pid=$(ps -ef | grep "java" | grep "pestcontrol.jar" | awk '{print $2}')
if [ ! -x ${pid} ]; then
   echo -e "Existing process found (${pid}) - is it running?"
   exit 1
fi

app_jarfile=pestcontrol.jar
if [ ! -f "$app_jarfile" ]; then
    app_jarfile=target/pestcontrol.jar
fi

if [ ! -f "$app_jarfile" ]; then
    echo -e "Building jar.."
    ./mvnw clean install
fi

nohup java -jar $app_jarfile $* > battery-stdout.log 2>&1 &

sleep 2

pid=$(ps -ef | grep "java" | grep "battery" | awk '{print $2}')

if [ -x ${pid} ]; then
   echo -e "No pestcontrol.jar process found - check battery-stdout.log"
   exit 1
else
   echo -e "Start successful - check battery-stdout.log"
   exit 0
fi
