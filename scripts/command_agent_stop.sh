#!/bin/bash

# ./cluster-admin agent-stop --sql-addr=localhost:26258
# ./cluster-admin agent-stop --sql-addr=localhost:26259
# ./cluster-admin agent-stop --sql-addr=localhost:26260

commandaction="Stop node (agent)"

for i in "$@"; do
  case $i in
    --sql-addr=*)
      sql_addr="${i#*=}"
      shift
      ;;
    -*|--*)
      echo "Unknown option $i"
      exit 1
      ;;
    *)
      ;;
  esac
done

fn_print_info "sql_addr = ${sql_addr}"

if [ -z "${sql_addr}" ]; then
  fn_print_error "Missing sql-addr parameter!"
  exit 1
fi

#
# Begin script
#

fn_print_dots "Stopping node [--sql-addr=${sql_addr}] in $security_mode mode"

fn_local_select_pid ${sql_addr}

if [ -z $pid ]; then
  fn_print_info "No cockroachdb process found"
  exit 0
fi

fn_local_stop "$pid"

fn_print_ok "Stopped node successfully"
