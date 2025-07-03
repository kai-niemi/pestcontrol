#!/bin/bash

getopt=$1
shift

fn_print_help() {
    echo -e "${green}Usage: $0 [command]${default}"
    echo -e "${default}Pest Control Admin Tool${default}"
    echo -e ""
    echo -e "${lightgreen}[Commands]${default}"
    {
        echo -e "${green}run-service\t${default}      | Run Pest Control service"
        echo -e "${green}start-service\t${default}      | Start Pest Control service"
        echo -e "${green}stop-service\t${default}      | Stop Pest Control service"
        echo -e "${green}start-lb\t${default}      | Start HAProxy load balancer"
        echo -e "${green}stop-lb\t${default}      | Stop HAProxy load balancer"
#        echo -e "${green}start-toxi\t${default}      | Start Toxiproxy server and add node proxies"
#        echo -e "${green}stop-toxi\t${default}      | Stop Toxiproxy server"
    } | column -s $'\t' -t
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
    run-service)
        command_run_service.sh $*
        ;;
    start-service)
        command_start_service.sh $*
        ;;
    start-lb)
        command_start_lb.sh $*
        ;;
    start-toxi)
        command_start_proxy.sh $*
        ;;
    stop-service)
        command_stop_service.sh $*
        ;;
    stop-lb)
        command_stop_lb.sh $*
        ;;
    stop-toxi)
        command_stop_proxy.sh $*
        ;;
    help|--help)
        fn_print_help
        ;;
    *)
        if [ -n "${getopt}" ]; then
            fn_print_help
            echo ""
            echo -e "${red}Unknown command${default}: $0 ${getopt}"
        else
           command_run_service.sh $*
        fi
esac
