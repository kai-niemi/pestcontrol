#!/bin/bash

./pop start --name=n1 --locality=region=eu-north-1,zone=eu-north-1a --sql-addr=127.0.0.1:26257 --listen-addr=127.0.0.1:25257 --advertise-addr=127.0.0.1:25257 --http-addr=127.0.0.1:8080 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259
./pop start --name=n2 --locality=region=eu-north-1,zone=eu-north-1b --sql-addr=127.0.0.1:26258 --listen-addr=127.0.0.1:25258 --advertise-addr=127.0.0.1:25258 --http-addr=127.0.0.1:8081 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259
./pop start --name=n3 --locality=region=eu-north-1,zone=eu-north-1c --sql-addr=127.0.0.1:26259 --listen-addr=127.0.0.1:25259 --advertise-addr=127.0.0.1:25259 --http-addr=127.0.0.1:8082 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259

#./pop start --name=n4 --locality=region=eu-central-1,zone=eu-central-1a --sql-addr=127.0.0.1:26260 --listen-addr=127.0.0.1:25260 --advertise-addr=127.0.0.1:25260 --http-addr=127.0.0.1:8083 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259
#./pop start --name=n5 --locality=region=eu-central-1,zone=eu-central-1b --sql-addr=127.0.0.1:26261 --listen-addr=127.0.0.1:25261 --advertise-addr=127.0.0.1:25261 --http-addr=127.0.0.1:8084 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259
#./pop start --name=n6 --locality=region=eu-central-1,zone=eu-central-1c --sql-addr=127.0.0.1:26262 --listen-addr=127.0.0.1:25262 --advertise-addr=127.0.0.1:25262 --http-addr=127.0.0.1:8085 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259

#./pop start --name=n7 --locality=region=eu-south-1,zone=eu-south-1a --sql-addr=127.0.0.1:26263 --listen-addr=127.0.0.1:25263 --advertise-addr=127.0.0.1:25263 --http-addr=127.0.0.1:8086 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259
#./pop start --name=n8 --locality=region=eu-south-1,zone=eu-south-1b --sql-addr=127.0.0.1:26264 --listen-addr=127.0.0.1:25264 --advertise-addr=127.0.0.1:25264 --http-addr=127.0.0.1:8087 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259
#./pop start --name=n9 --locality=region=eu-south-1,zone=eu-south-1c --sql-addr=127.0.0.1:26265 --listen-addr=127.0.0.1:25265 --advertise-addr=127.0.0.1:25265 --http-addr=127.0.0.1:8088 --join=127.0.0.1:25257,127.0.0.1:25258,127.0.0.1:25259

./pop init --sql-addr=127.0.0.1:26257 --advertise-addr=127.0.0.1:25257

#./pop gen-haproxy
#./pop start-haproxy

./pop sql --sql-addr=127.0.0.1:26257
