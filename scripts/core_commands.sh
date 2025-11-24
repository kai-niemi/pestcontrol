#!/bin/bash
# Core commands

core_exit.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

core_functions.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

# Commands used from spring shell or agents

command_start_haproxy.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop_haproxy.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start_toxiproxy.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop_toxiproxy.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_cert.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_node_cert.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_init.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_install.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start_toxiproxy.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop_proxy.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_kill.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_sql.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_status.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

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

command_getopt.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}
