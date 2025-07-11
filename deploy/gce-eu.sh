#!/bin/bash
# Configuration
########################

title="CockroachDB multi region deployment (GCE)"
# CRDB release version
releaseversion="v25.2.2"
# Number of node instances in total including clients
nodes="12"
# Nodes hosting CRDB
crdbnodes="1-9"
# Array of client nodes (must match size of regions)
clientnodes="10-12"
# Array of regions localities (must match zone names)
regions="europe-west1,europe-west2,europe-west3"
# AWS/GCE cloud (aws|gce)
cloud="gce"
# AWS/GCE region zones (must align with nodes count)
zones="\
europe-west1-b,\
europe-west1-c,\
europe-west1-d,\
europe-west2-a,\
europe-west2-b,\
europe-west2-c,\
europe-west3-a,\
europe-west3-b,\
europe-west3-c,\
europe-west1-b,\
europe-west2-a,\
europe-west3-a"
# AWS/GCE machine types
machinetypes="n2-standard-4"
# Secure (default) or insecure cluster
insecure=on
# Dry run mode
dryrun=on

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