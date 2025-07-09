#!/bin/bash
# Commands for control plane (orchestrating host)

command_run_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

control_getopt.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}
