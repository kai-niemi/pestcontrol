#!/bin/bash

regions="\
eu-central-1,\
eu-central-1,\
eu-central-1"

zones="\
eu-central-1a,\
eu-central-1b,\
eu-central-1c"

ip_internal="\
10.6.6.12,\
10.6.28.206,\
10.6.38.225"

echo "gen-cfg --internalIPs $ip_internal --regions $regions --zones $zones" > test.txt
echo "quit" >> test.txt

java -jar target/pestcontrol.jar @test.txt
