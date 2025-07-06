#!/bin/bash
# Commands used from spring shell or agents

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

command_kill.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_sql.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

shell_getopt.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}
