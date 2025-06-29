#!/bin/bash

curl -X POST -F "files=@.certs/ca.crt" -F "files=@.certs/node.crt" -F "files=@.certs/node.key" http://localhost:9090/api/cluster/certs
