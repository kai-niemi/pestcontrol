#!/bin/bash

core_util.sh

fn_echo_header
{
	echo -e "${lightblue}Cluster id:\t\t${default}$CLUSTER"
	echo -e "${lightblue}Node count:\t\t${default}$nodes"
	echo -e "${lightblue}CRDB nodes:\t\t${default}$crdbnodes"
	echo -e "${lightblue}Client nodes:\t\t${default}${clientnodes}"
	echo -e "${lightblue}Version:\t\t${default}$releaseversion"
	echo -e "${lightblue}Cloud:\t\t${default}$cloud"
	echo -e "${lightblue}Machine types:\t\t${default}$machinetypes"
	echo -e "${lightblue}Regions:\t\t${default}$regions"
	echo -e "${lightblue}Zones:\t\t${default}$zones"
	echo -e "${lightblue}Insecure mode:\t\t${default}$insecure"
	echo -e "${lightblue}Dryrun:\t\t${default}$dryrun"
} | column -s $'\t' -t

if [ -z "${CLUSTER}" ]; then
  fn_echo_warn_nl "No \$CLUSTER id variable set!"
  echo "Use: export CLUSTER='your-cluster-id'"
  exit 1
fi

assembly_path=${basedir}/target/pestcontrol.tar.gz

if [ ! -f ${assembly_path} ]; then
  fn_echo_warn_nl "Not found: ${assembly_path}"
  exit 1
fi

fn_split_array ${clientnodes}
clientnodes_arr=("${OUT[@]}")

fn_split_array ${crdbnodes}
crdbnodes_arr=("${OUT[@]}")

IFS=',' read -ra regions_arr <<< "$regions"
IFS=',' read -ra zones_arr <<< "$zones"

echo "Node array: (${crdbnodes})"
for value in "${crdbnodes_arr[@]}" ; do
  echo -e "${lightyellow}$value${default}"
done

echo "Client array: (${clientnodes})"
for value in "${clientnodes_arr[@]}" ; do
  echo -e "${lightyellow}$value${default}"
done

echo "Region array: (${regions})"
for value in "${regions_arr[@]}" ; do
  echo -e "${lightyellow}$value${default}"
done

echo "Zone array: (${zones})"
for value in "${zones_arr[@]}" ; do
  echo -e "${lightyellow}$value${default}"
done

create_cluster.sh
