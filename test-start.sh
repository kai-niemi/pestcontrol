#!/bin/bash

./pop start --name=n1 --locality=region=eu-north-1,zone=eu-north-1a --sql-addr=localhost:26257 --listen-addr=localhost:25257 --advertise-addr=localhost:25257 --http-addr=localhost:8080 --join=localhost:25257,localhost:25258,localhost:25259
./pop start --name=n2 --locality=region=eu-north-1,zone=eu-north-1b --sql-addr=localhost:26258 --listen-addr=localhost:25258 --advertise-addr=localhost:25258 --http-addr=localhost:8081 --join=localhost:25257,localhost:25258,localhost:25259
./pop start --name=n3 --locality=region=eu-north-1,zone=eu-north-1c --sql-addr=localhost:26259 --listen-addr=localhost:25259 --advertise-addr=localhost:25259 --http-addr=localhost:8082 --join=localhost:25257,localhost:25258,localhost:25259

#./pop start --name=n4 --locality=region=eu-central-1,zone=eu-central-1a --sql-addr=localhost:26260 --listen-addr=localhost:25260 --advertise-addr=localhost:25260 --http-addr=localhost:8083 --join=localhost:25257,localhost:25258,localhost:25259
#./pop start --name=n5 --locality=region=eu-central-1,zone=eu-central-1b --sql-addr=localhost:26261 --listen-addr=localhost:25261 --advertise-addr=localhost:25261 --http-addr=localhost:8084 --join=localhost:25257,localhost:25258,localhost:25259
#./pop start --name=n6 --locality=region=eu-central-1,zone=eu-central-1c --sql-addr=localhost:26262 --listen-addr=localhost:25262 --advertise-addr=localhost:25262 --http-addr=localhost:8085 --join=localhost:25257,localhost:25258,localhost:25259

#./pop start --name=n7 --locality=region=eu-south-1,zone=eu-south-1a --sql-addr=localhost:26263 --listen-addr=localhost:25263 --advertise-addr=localhost:25263 --http-addr=localhost:8086 --join=localhost:25257,localhost:25258,localhost:25259
#./pop start --name=n8 --locality=region=eu-south-1,zone=eu-south-1b --sql-addr=localhost:26264 --listen-addr=localhost:25264 --advertise-addr=localhost:25264 --http-addr=localhost:8087 --join=localhost:25257,localhost:25258,localhost:25259
#./pop start --name=n9 --locality=region=eu-south-1,zone=eu-south-1c --sql-addr=localhost:26265 --listen-addr=localhost:25265 --advertise-addr=localhost:25265 --http-addr=localhost:8088 --join=localhost:25257,localhost:25258,localhost:25259

./pop init --sql-addr=localhost:26257 --advertise-addr=localhost:25257
./pop sql --sql-addr=localhost:26257
