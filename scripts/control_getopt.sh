#!/bin/bash

getopt=$1
shift

fn_print_help() {
    echo -e "${green}Usage: $0 [command]${default}"
    echo -e "${default}Pest Control${default}"
    echo -e ""
    echo -e "${lightgreen}[Commands]${default}"
    {
        echo -e "${green}run-service\t${default}      | Run Pest Control service"
        echo -e "${green}start-service\t${default}      | Start Pest Control service"
        echo -e "${green}stop-service\t${default}      | Stop Pest Control service"
    } | column -s $'\t' -t
}

case "${getopt}" in
    run-service)
        command_run_service.sh $*
        ;;
    start-service)
        command_start_service.sh $*
        ;;
    stop-service)
        command_stop_service.sh $*
        ;;
    help|--help)
        fn_print_help
        ;;
    *)
        if [ -n "${getopt}" ]; then
            fn_print_help
            echo -e "${red}Unknown command${default}: $0 ${getopt}"
        else
           command_run_service.sh $*
        fi
esac
