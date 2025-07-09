#!/bin/bash

commandaction="Deploy Clients"

fn_failcheck roachprod put ${CLUSTER}:${clientnodes} ${basedir}/target/${assembly}.tar.gz ${assembly}.tar.gz
fn_failcheck roachprod run ${CLUSTER}:${clientnodes} "tar xvf ${assembly}.tar.gz"

fn_echo_info_nl "done"
