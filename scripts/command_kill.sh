#!/bin/bash

commandaction="Kill node "

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

#
# Begin script
#

if [ -z $pid ]; then
    fn_print_error "No cockroachdb process found"
    exit 1
fi

fn_print_dots "Killing node with pid ${pid}"

kill -KILL ${pid}

fn_print_dots "Waiting for server to stop (pid: $pid)"

while kill -0 $pid 2>/dev/null; do
    printf '.'
    sleep 1
done

fn_print_ok "Killed (pid: $pid)"

exit 0