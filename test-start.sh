#!/bin/bash

./pest-operator start --name=n1 --locality=region=eu-north-1,zone=eu-north-1a --listen-addr=localhost:26257 --advertise-addr=localhost:26257 --http-addr=localhost:8080 --join=localhost:25257,localhost:25258,localhost:25259
./pest-operator start --name=n2 --locality=region=eu-north-1,zone=eu-north-1b --listen-addr=localhost:26258 --advertise-addr=localhost:26258 --http-addr=localhost:8081 --join=localhost:25257,localhost:25258,localhost:25259
./pest-operator start --name=n3 --locality=region=eu-north-1,zone=eu-north-1c --listen-addr=localhost:26259 --advertise-addr=localhost:26259 --http-addr=localhost:8082 --join=localhost:25257,localhost:25258,localhost:25259

./pest-operator init --sql-addr=localhost:26257 --advertise-addr=localhost:26257
./pest-operator sql --sql-addr=localhost:26257
