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

fn_stage_clients() {
  fn_echo_info_nl "Stage clients"

  security_mode=""
  if [ "${insecure}" == "on" ]; then
    security_mode="--insecure"
  fi

  fn_failcheck roachprod run $security_mode ${CLUSTER}:$clientnodes 'sudo apt-get -qq update'
  fn_failcheck roachprod run $security_mode ${CLUSTER}:$clientnodes 'sudo apt-get -qq install -y openjdk-21-jre-headless htop dstat haproxy'

  fn_failcheck roachprod run $security_mode ${CLUSTER}:$crdbnodes 'sudo apt-get -qq update'
  fn_failcheck roachprod run $security_mode ${CLUSTER}:$crdbnodes 'sudo apt-get -qq install -y openjdk-21-jre-headless'
}

fn_deploy_agents() {
  fn_echo_info_nl "Deploy agents to all nodes"

  security_mode=""
  if [ "${insecure}" == "on" ]; then
    security_mode="--insecure"
  fi

  fn_failcheck roachprod put $security_mode ${CLUSTER}:$clientnodes $basedir/target/pestcontrol.tar.gz pestcontrol.tar.gz
  fn_failcheck roachprod run $security_mode ${CLUSTER}:$clientnodes "tar xvf pestcontrol.tar.gz"

  fn_failcheck roachprod put $security_mode ${CLUSTER}:$crdbnodes $basedir/target/pestcontrol.tar.gz pestcontrol.tar.gz
  fn_failcheck roachprod run $security_mode ${CLUSTER}:$crdbnodes "tar xvf pestcontrol.tar.gz"
}

fn_start_agents() {
  fn_echo_info_nl "Start agents on crdb nodes"

  for c in $crdbnodes_arr
  do
    if [ "${insecure}" == "on" ]; then
      fn_failcheck roachprod run --insecure ${CLUSTER}:$c "cd pestcontrol && ./pest-control start-service --profiles cloud --cluster-id=cloud-insecure"
    else
      fn_failcheck roachprod run ${CLUSTER}:$c "cd pestcontrol && ./pest-control start-service --profiles cloud --cluster-id=cloud-secure"
    fi
  done
}

fn_deploy_cfg() {
  fn_echo_info_nl "Deploy application config with IP lists to client nodes"

  # Write node IPs to text files
  security_mode=""
  if [ "${insecure}" == "on" ]; then
    security_mode="--insecure"
  fi

  ip_internal=$(roachprod ip $CLUSTER)
  ip_external=$(roachprod ip $CLUSTER --external)
#  value=$(<config.txt)

  $basedir/target/pestcontrol.jar --generate \
   --zones $zones \
   --ip-internal $ip_internal \
   --ip-external $ip_external > application-cloud.yml

  fn_failcheck roachprod put $security_mode ${CLUSTER}:$clientnodes application-cloud.yml pestcontrol/config/application-cloud.yml
}

if fn_prompt_yes_no "Create cluster?" Y; then
  fn_create_cluster
fi

if fn_prompt_yes_no "Stage clients?" Y; then
  fn_stage_clients
fi

if fn_prompt_yes_no "Deploy agents?" Y; then
  fn_deploy_agents
fi

if fn_prompt_yes_no "Start agents?" Y; then
  fn_start_agents
fi

fn_deploy_ip

fn_echo_info_nl "Done"