#!/bin/bash

getopt=$1
shift

fn_print_help() {
    echo -e "${green}Usage: $0 [command]${default}"
    echo -e "${default}Pest Control Shell Script${default}"
    echo -e ""
    echo -e "${yellow}Note: This script is intended to be used from spring shell via RPC${default}"
    echo -e ""
}

case "${getopt}" in
    cert)
        command_cert.sh $*
        ;;
    node-cert)
        command_node_cert.sh $*
        ;;
    start)
        command_start.sh $*
        ;;
    stop)
        command_stop.sh $*
        ;;
    kill)
        command_kill.sh $*
        ;;
    install)
        command_install.sh $*
        ;;
    init)
        command_init.sh $*
        ;;
    sql)
        command_sql.sh $*
        ;;
    wipe)
        command_wipe.sh $*
        ;;
    help|--help|*)
        fn_print_help
        ;;
esac

if [ -n "${getopt}" ]; then
    fn_print_help
    echo ""
    echo -e "${red}Unknown command${default}: $0 ${getopt}"
fi
