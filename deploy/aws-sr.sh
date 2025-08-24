#!/bin/bash
# Configuration
########################

cluster_id="cloud-insecure"

title="CockroachDB single region deployment (AWS)"
# CRDB release version
releaseversion="v25.3.0"
# Number of node instances in total including clients
nodes="4"
# Node id range hosting CRDB
crdbnodes="1-3"
# Node id range hosting clients (must match n:of regions)
clientnodes="4"
# Range of region localities (must match zone names)
regions="eu-central-1"
# AWS/GCE cloud (aws|gce)
cloud="aws"
# Cloud region zones, first item denotes node 1 (must match nodes number)
zones="\
eu-central-1a,\
eu-central-1b,\
eu-central-1c,\
eu-central-1a"
# AWS/GCE machine types
# https://aws.amazon.com/ec2/instance-types/m6i/
machinetypes="m6i.large"
# Secure or insecure cluster
insecure=on
# Dry run mode (no actual cluster creation just print commands)
dryrun=off

# DO NOT EDIT BELOW THIS LINE
#############################

case "$OSTYPE" in
  darwin*)
    rootdir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
    selfname=$(basename "$(readlink -f "${BASH_SOURCE[0]}")")
    ;;
  *)
    rootdir="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"
    selfname=$(basename "$(readlink -f "${BASH_SOURCE[0]}")")
    ;;
esac

if [ "$(whoami)" == "root" ]; then
    echo -e "[ FAIL ] Do NOT run as root!"
    exit 1
fi

basedir="${rootdir}/.."
functionsdir="${rootdir}/scripts"

source "${functionsdir}/core_functions.sh"

main.sh