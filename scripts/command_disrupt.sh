#!/bin/bash

if [ $# -eq 0 ]; then
    fn_print_error "Expected http-port"
    exitcode="1"
    core_exit.sh
fi

command_kill.sh $1 1

exitcode="0"
core_exit.sh