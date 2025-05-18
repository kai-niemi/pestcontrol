#!/bin/bash

core_util.sh(){
	modulefile="${FUNCNAME[0]}"
	source "${functionsdir}/${modulefile}"
}

create_cluster.sh(){
	modulefile="${FUNCNAME[0]}"
	source "${functionsdir}/${modulefile}"
}

deploy_clients.sh(){
	modulefile="${FUNCNAME[0]}"
	source "${functionsdir}/${modulefile}"
}

main.sh(){
	modulefile="${FUNCNAME[0]}"
	source "${functionsdir}/${modulefile}"
}

