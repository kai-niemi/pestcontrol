#!/bin/bash

commandaction="Create Cluster"

fn_create_cluster() {
  if [ "${cloud}" = "aws" ]; then
    fn_failcheck roachprod create $CLUSTER --clouds=aws \
    --aws-machine-type-ssd=${machinetypes} --aws-zones=${zones} \
    --aws-profile crl-revenue --aws-config ~/rev.json \
    --geo --local-ssd-no-ext4-barrier \
    --nodes=${nodes} \
    --os-volume-size 750 \
    --lifetime 24h0m0s
  elif [ "${cloud}" = "gce" ]; then
    fn_failcheck roachprod create $CLUSTER --clouds=gce \
    --gce-machine-type=${machinetypes} --gce-zones=${zones} \
    --geo --local-ssd-no-ext4-barrier \
    --nodes=${nodes} \
    --os-volume-size 750 \
    --lifetime 24h0m0s
  fi
}

fn_stage_cluster() {
  fn_echo_info_nl "Stage binaries $releaseversion"

  fn_failcheck roachprod stage $CLUSTER release $releaseversion
}

fn_stage_clients() {
  fn_echo_info_nl "Stage clients ${CLUSTER}:$nodes"

  if [ "${insecure}" == "on" ]; then
    fn_failcheck roachprod run --insecure ${CLUSTER}:$nodes 'sudo apt-get -qq update'
    fn_failcheck roachprod run --insecure ${CLUSTER}:$nodes 'sudo apt-get -qq install -y openjdk-21-jre-headless htop dstat haproxy'
  else
    fn_failcheck roachprod run ${CLUSTER}:$nodes 'sudo apt-get -qq update'
    fn_failcheck roachprod run ${CLUSTER}:$nodes 'sudo apt-get -qq install -y openjdk-21-jre-headless htop dstat haproxy'
  fi

  fn_failcheck roachprod put ${CLUSTER}:${nodes} ${basedir}/target/pestcontrol.tar.gz pestcontrol.tar.gz
  fn_failcheck roachprod run ${CLUSTER}:${nodes} "tar xvf pestcontrol.tar.gz"
}

fn_start_clients() {
  fn_echo_info_nl "Start services ${CLUSTER}:$nodes with cluster id $cluster_id"

  i=0;
  for c in $nodes_arr
  do
    if [ "${insecure}" == "on" ]; then
      fn_failcheck roachprod run --insecure ${CLUSTER}:$c "./pest-control --cluster-id=$cluster_id"
    else
      fn_failcheck roachprod run ${CLUSTER}:$c "./pest-control --cluster-id=$cluster_id"
    fi
  done
}

##################################################################

if fn_prompt_yes_no "Create cluster?" Y; then
  fn_create_cluster
fi

if fn_prompt_yes_no "Stage clients?" Y; then
  fn_stage_cluster
fi

if fn_prompt_yes_no "Start clients?" Y; then
  fn_start_clients
fi

roachprod ip $CLUSTER --external

fn_echo_info_nl "Done"