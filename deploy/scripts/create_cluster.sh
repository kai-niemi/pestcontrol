#!/bin/bash

commandaction="Create Cluster"

fn_create_cluster() {
  if [ "${cloud}" = "aws" ]; then
    fn_failcheck roachprod create $CLUSTER --clouds=aws \
    --aws-machine-type-ssd=${machinetypes} --aws-zones=${zones} \
    --aws-profile crl-revenue --aws-config ~/rev.json \
    --geo --local-ssd-no-ext4-barrier \
    --nodes=${nodes} \
    --os-volume-size 750 --lifetime 24h0m0s
  elif [ "${cloud}" = "gce" ]; then
    fn_failcheck roachprod create $CLUSTER --clouds=gce \
    --gce-machine-type=${machinetypes} --gce-zones=${zones} \
    --geo --local-ssd-no-ext4-barrier \
    --nodes=${nodes} \
    --os-volume-size 750 --lifetime 24h0m0s
  fi
}

fn_stage_cluster() {
  fn_echo_info_nl "Stage binaries $releaseversion"

  fn_failcheck roachprod stage $CLUSTER release $releaseversion
}

fn_start_cluster() {
  fn_echo_info_nl "Start CockroachDB nodes $crdbnodes"

  if [ "${insecure}" == "on" ]; then
    fn_failcheck roachprod start --insecure $CLUSTER:$crdbnodes
    fn_failcheck roachprod admin --insecure --open --ips $CLUSTER:1
  else
    fn_failcheck roachprod start $CLUSTER:$crdbnodes
    fn_failcheck roachprod admin --open --ips $CLUSTER:1
  fi
}

fn_stage_clients() {
  fn_echo_info_nl "Stage clients ${CLUSTER}:$clientnodes"

  i=0;
  for c in $clientnodes_arr
  do
    region=${regions_arr[$i]}
    i=($i+1)

    echo -e "Client: $c Region: $region"

    if [ "${insecure}" == "on" ]; then
      fn_failcheck roachprod run --insecure ${CLUSTER}:$c "./cockroach gen haproxy --insecure --host $(roachprod ip $CLUSTER:1 --external) --locality=region=$region"
    else
      fn_failcheck roachprod run ${CLUSTER}:$c "./cockroach gen haproxy --certs-dir=certs --host $(roachprod ip $CLUSTER:1 --external) --locality=region=$region"
    fi
  done

  if [ "${insecure}" == "on" ]; then
    fn_failcheck roachprod run --insecure ${CLUSTER}:$clientnodes 'sudo apt-get -qq update'
    fn_failcheck roachprod run --insecure ${CLUSTER}:$clientnodes 'sudo apt-get -qq install -y openjdk-21-jre-headless htop dstat haproxy'
    fn_failcheck roachprod run --insecure ${CLUSTER}:$clientnodes 'nohup haproxy -f haproxy.cfg > /dev/null 2>&1 &'
  else
    fn_failcheck roachprod run ${CLUSTER}:$clientnodes 'sudo apt-get -qq update'
    fn_failcheck roachprod run ${CLUSTER}:$clientnodes 'sudo apt-get -qq install -y openjdk-21-jre-headless htop dstat haproxy'
    fn_failcheck roachprod run ${CLUSTER}:$clientnodes 'nohup haproxy -f haproxy.cfg > /dev/null 2>&1 &'
  fi
}

fn_create_db() {
  fn_echo_info_nl "Creating database via $CLUSTER:1"

if [ "${insecure}" == "on" ]; then
fn_failcheck roachprod run --insecure $CLUSTER:1 <<EOF
./cockroach sql --insecure --host=`roachprod ip $CLUSTER:1` -e "CREATE DATABASE if not exists pestcontrol; CREATE USER IF NOT EXISTS craig;"
EOF
else
fn_failcheck roachprod run $CLUSTER:1 <<EOF
./cockroach sql --certs-dir=certs --host=`roachprod ip $CLUSTER:1` -e "CREATE DATABASE if not exists pestcontrol; CREATE USER IF NOT EXISTS craig WITH PASSWORD 'cockroach'; ALTER ROLE root WITH PASSWORD 'cockroach'; ALTER ROLE craig WITH PASSWORD 'cockroach';"
EOF
fi
}

##################################################################

if fn_prompt_yes_no "Create new cluster?" Y; then
  fn_create_cluster
  fn_stage_cluster
  fn_start_cluster
  fn_stage_clients
  fn_create_db
else
  if fn_prompt_yes_no "Step 1/4: Stage cluster?" Y; then
    fn_stage_cluster
  fi

  if fn_prompt_yes_no "Step 2/4: Start cluster?" Y; then
    fn_start_cluster
  fi

  if fn_prompt_yes_no "Step 3/4: Stage clients?" Y; then
    fn_stage_clients
  fi

  if fn_prompt_yes_no "Step 4/4: Create DB?" Y; then
    fn_create_db
  fi
fi

fn_echo_info_nl "done"