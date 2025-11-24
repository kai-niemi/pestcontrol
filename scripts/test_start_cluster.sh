#!/bin/bash

#killall -9 cockroach

.binaries/cockroach start \
    --locality=region=eu-north-1,zone=eu-north-1a \
    --listen-addr=:25257 \
    --advertise-addr=localhost:25257 \
    --sql-addr=:26257 \
    --http-addr=:8080 \
    --join=localhost:25257,localhost:25258,localhost:25259 \
    --store=.datafiles/n1  --cache=10% --max-sql-memory=10% --background --insecure

.binaries/cockroach start \
    --locality=region=eu-north-1,zone=eu-north-1b \
    --listen-addr=:25258 \
    --advertise-addr=localhost:25258 \
    --sql-addr=:26258 \
    --http-addr=:8081 \
    --join=localhost:25257,localhost:25258,localhost:25259 \
    --store=.datafiles/n2  --cache=10% --max-sql-memory=10% --background --insecure

.binaries/cockroach start \
    --locality=region=eu-north-1,zone=eu-north-1c \
    --listen-addr=:25259 \
    --advertise-addr=localhost:25259 \
    --sql-addr=:26259 \
    --http-addr=:8082 \
    --join=localhost:25257,localhost:25258,localhost:2259 \
    --store=.datafiles/n3  --cache=10% --max-sql-memory=10% --background --insecure

.binaries/cockroach init --host=localhost:25258 --insecure
.binaries/cockroach sql --host=localhost:25258 --insecure