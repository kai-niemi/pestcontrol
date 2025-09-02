#!/bin/bash

getopt=$1
shift

fn_print_help() {
    echo -e "${green}Usage: $0 [command]${default}"
    echo -e "${default}Pest Control :: Operator Commands${default}"
    echo -e ""
    echo -e "${yellow}Internal commands are intended to be called programmatically via RPC from the built-in shell.${default}"
    echo -e ""
    echo -e "${lightgreen}[Internal Commands]${default}"
    {
        echo -e "${yellow}start\t${default}      | Start node"
        echo -e "${yellow}stop\t${default}      | Stop node"
        echo -e "${yellow}kill\t${default}      | Kill node"
        echo -e "${yellow}init\t${default}      | Init node"
        echo -e "${yellow}sql\t${default}      | Start SQL client"
        echo -e "${green}cert\t${default}      | Generate CA cert and key pairs"
        echo -e "${green}node-cert\t${default}      | Generate node cert and key pairs"
        echo -e "${green}install\t${default}      | Install cockroachdb binaries"
        echo -e "${green}wipe\t${default}      | Wipe local directories (certs, data and binaries)"
        echo -e "${cyan}start-proxy\t${default}      | Start ToxiProxy server"
        echo -e "${cyan}stop-proxy\t${default}      | Stop ToxiProxy server"
        echo -e "${cyan}start-haproxy\t${default}      | Start HAProxy load balancer"
        echo -e "${cyan}stop-haproxy\t${default}      | Stop HAProxy load balancer"
    } | column -s $'\t' -t

    echo -e ""
    echo -e "${lightgreen}[Service Commands]${default}"
    {
        echo -e "${cyan}start-service\t${default}    | Start PestControl service in background"
        echo -e "${cyan}stop-service\t${default}    | Stop PestControl service"
        echo -e "${cyan}run-service\t${default}    | Run PestControl service"
    } | column -s $'\t' -t
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
    cert)
        command_cert.sh $*
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
    start-proxy)
        command_start_proxy.sh $*
        ;;
    stop-proxy)
        command_stop_proxy.sh $*
        ;;
    gen-haproxy)
        command_gen_haproxy.sh $*
        ;;
    start-haproxy)
        command_start_haproxy.sh $*
        ;;
    stop-haproxy)
        command_stop_haproxy.sh $*
        ;;
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
        fn_print_help
        if [ -n "${getopt}" ]; then
            echo -e ""
            echo -e "${red}Unknown command${default}: $0 ${getopt}"
        fi
esac
