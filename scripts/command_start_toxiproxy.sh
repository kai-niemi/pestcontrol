#!/bin/bash

commandaction="Start toxiproxy server"

# Toxiproxy server host
toxiproxy_host=localhost
# Toxiproxy server port
toxiproxy_port=8474

for i in "$@"; do
  case $i in
    --toxiproxy-host=*)
      toxiproxy_host="${i#*=}"
      shift
      ;;
    --toxiproxy-port=*)
      toxiproxy_port="${i#*=}"
      shift
      ;;
    -*|--*)
      fn_print_warn "Unknown option $i"
      ;;
    *)
      ;;
  esac
done

fn_print_info "toxiproxy_host  = ${toxiproxy_host}"
fn_print_info "toxiproxy_port  = ${toxiproxy_port}"

if [ ! "$(command -v toxiproxy-server 2> /dev/null)" ]; then
	echo -e "[ FAIL ] toxiproxy is not installed"
	exit 1
fi

pid=$(ps -ef | grep "toxiproxy-server" | grep "host" | awk '{print $2}')
if [ ! -x ${pid} ]; then
   fn_print_info "toxiproxy-server (pid: ${pid}) appears to be running"
   exit 0
fi

mkdir -p ${logdir}

fn_fail_check nohup toxiproxy-server -host ${toxiproxy_host} -port ${toxiproxy_port} > ${logdir}/toxiproxy-stdout.log 2>&1 &

fn_print_ok "Started toxiproxy-server - check ${logdir}/toxiproxy-stdout.log"