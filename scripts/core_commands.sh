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
