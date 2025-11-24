#!/bin/bash

commandaction="Stop node"

for i in "$@"; do
  case $i in
    --listen-addr=*)
      listen_addr="${i#*=}"
      pid=$(ps -ef | grep "cockroach" | grep "listen-addr=${listen_addr}" | awk '{print $2}')
      shift
      ;;
    --advertise-addr=*)
      advertise_addr="${i#*=}"
      pid=$(ps -ef | grep "cockroach" | grep "advertise-addr=${advertise_addr}" | awk '{print $2}')
      shift
      ;;
    --sql-addr=*)
      sql_addr="${i#*=}"
      pid=$(ps -ef | grep "cockroach" | grep "sql-addr=${sql_addr}" | awk '{print $2}')
      shift
      ;;
    --http-addr=*)
      http_addr="${i#*=}"
      pid=$(ps -ef | grep "cockroach" | grep "http-addr=${http_addr}" | awk '{print $2}')
      shift
      ;;
    -*|--*)
      fn_print_warn "Unknown option $i"
      ;;
    *)
      ;;
  esac
done

fn_print_info "listen_addr    = ${listen_addr}"
fn_print_info "advertise_addr = ${advertise_addr}"
fn_print_info "sql_addr       = ${sql_addr}"
fn_print_info "http_addr      = ${http_addr}"
fn_print_info "pid            = ${pid}"

#
# Begin script
#

if [ -z $pid ]; then
    fn_print_error "No cockroachdb process found"
    exit 1
fi

fn_print_dots "Stopping node with pid ${pid}"

kill -TERM ${pid}

fn_print_dots "Waiting for server to stop (pid: $pid)"

let attempts=0
while kill -0 $pid 2>/dev/null; do
    printf '.'
    sleep 2
    let attempts=attempts+1
    if [ ${attempts} -gt 5 ]; then
      fn_print_warn "Giving up waiting (${attempts}) - issuing SIGKILL (pid: $pid)"
      kill -KILL ${pid}
      break
    fi
done

fn_print_ok "Stopped (pid: $pid)"

exit 0