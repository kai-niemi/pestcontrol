#!/bin/bash

# ./cluster-admin agent-stop --listen-addr=localhost:26258
# ./cluster-admin agent-stop --listen-addr=localhost:26259
# ./cluster-admin agent-stop --listen-addr=localhost:26260

commandaction="Stop node (agent)"

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
    fn_local_pids
    fn_print_error "No cockroachdb process found"
    exit 1
fi

fn_print_dots "Stopping node with pid ${pid}"

fn_local_stop "$pid"

fn_print_ok "Stopped node successfully"
