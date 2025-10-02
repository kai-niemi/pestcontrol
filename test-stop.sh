#!/bin/bash

./pop kill --listen-addr=127.0.0.1:25257 --advertise-addr=127.0.0.1:25257
./pop kill --listen-addr=127.0.0.1:25258 --advertise-addr=127.0.0.1:25258
./pop kill --listen-addr=127.0.0.1:25259 --advertise-addr=127.0.0.1:25259
./pop stop-haproxy