#!/bin/bash

getopt=$1
shift

fn_print_help() {
    echo -e "${green}Usage: $0 <command> [<args>]${default}"
    echo -e "${default}Pest Control Operator Commands${default}"
    echo -e ""
    echo -e "${cyan}Internal commands called programmatically via RPC from the built-in shell.${default}"
    {
        echo -e "${darkgrey}  start\t      Start node"
        echo -e "${darkgrey}  stop\t      Stop node"
        echo -e "${darkgrey}  kill\t      Kill node"
        echo -e "${darkgrey}  init\t      Init node"
        echo -e "${darkgrey}  sql\t      Start SQL client"
        echo -e "${darkgrey}  status\t      Query node status"
        echo -e "${darkgrey}  cert\t      Generate CA cert and key pairs"
        echo -e "${darkgrey}  login\t      Login to get Cluster API authentication token (cookie)"
        echo -e "${darkgrey}  node-cert\t      Generate node cert and key pairs"
        echo -e "${darkgrey}  install\t      Install cockroachdb binaries"
        echo -e "${darkgrey}  start-toxiproxy\t      Start Toxiproxy server"
        echo -e "${darkgrey}  stop-toxiproxy\t      Stop Toxiproxy server"
        echo -e "${darkgrey}  start-haproxy\t      Start HAProxy load balancer"
        echo -e "${darkgrey}  stop-haproxy\t      Stop HAProxy load balancer"
    } | column -s $'\t' -t

    echo -e ""
    echo -e "${cyan}Operator commands to start, stop and run pestcontrol${default}"
    {
        echo -e "${lightyellow}  start-service\t${default}        Start server in non-interactive mode in the background"
        echo -e "${lightyellow}  stop-service\t${default}        Stop server"
        echo -e "${lightyellow}  run\t${default}        Run in interactive or non-interactive mode"
    } | column -s $'\t' -t

    echo -e ""
    echo -e "Execute './pest' and type 'help' for an overview of the system."
}

case "${getopt}" in
    start)
        command_start.sh $*
        ;;
    stop)
        command_stop.sh $*
        ;;
    kill)
        command_kill.sh $*
        ;;
    init)
        command_init.sh $*
        ;;
    sql)
        command_sql.sh $*
        ;;
    status)
        command_status.sh $*
        ;;
    cert)
        command_cert.sh $*
        ;;
    login)
        command_login.sh $*
        ;;
    node-cert)
        command_node_cert.sh $*
        ;;
    install)
        command_install.sh $*
        ;;
    wipe)
        command_wipe.sh $*
        ;;
    start-toxiproxy)
        command_start_toxiproxy.sh $*
        ;;
    stop-toxiproxy)
        command_stop_toxiproxy.sh $*
        ;;
    start-haproxy)
        command_start_haproxy.sh $*
        ;;
    stop-haproxy)
        command_stop_haproxy.sh $*
        ;;
    start-service)
        command_start_service.sh $*
        ;;
    stop-service)
        command_stop_service.sh $*
        ;;
    run-service|run)
        command_run_service.sh $*
        ;;
    open)
        command_open.sh $*
        ;;
    help|--help)
        fn_print_help
        ;;
    *)
        fn_print_help
        if [ -n "${getopt}" ]; then
            echo -e ""
            echo -e "${red}Unknown command${default}: $0 ${getopt}"
        fi
esac
