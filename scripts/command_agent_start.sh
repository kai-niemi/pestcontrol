#!/bin/bash

commandaction="Start node (agent)"

# ./cluster-admin agent-start --name=n1 --locality=region=eu-north-1,zone=eu-north-1a --listen-addr=localhost:25258 --advertise-addr=localhost:25258 --sql-addr=localhost:26258 --http-addr=localhost:8081 --join=localhost:25258,localhost:25259,localhost:25260
# ./cluster-admin agent-start --name=n2 --locality=region=eu-north-1,zone=eu-north-1b --listen-addr=localhost:25259 --advertise-addr=localhost:25259 --sql-addr=localhost:26259 --http-addr=localhost:8082 --join=localhost:25258,localhost:25259,localhost:25260
# ./cluster-admin agent-start --name=n3 --locality=region=eu-north-1,zone=eu-north-1c --listen-addr=localhost:25260 --advertise-addr=localhost:25260 --sql-addr=localhost:26260 --http-addr=localhost:8083 --join=localhost:25258,localhost:25259,localhost:25260

# https://www.cockroachlabs.com/docs/stable/cockroach-start#flags

for i in "$@"; do
  case $i in
    --name=*)
      name="${i#*=}"
      shift
      ;;
    --locality=*)
      locality="${i#*=}"
      shift
      ;;
    --listen-addr=*)
      listen_addr="${i#*=}"
      shift
      ;;
    --advertise-addr=*)
      advertise_addr="${i#*=}"
      shift
      ;;
    --sql-addr=*)
      sql_addr="${i#*=}"
      shift
      ;;
    --http-addr=*)
      http_addr="${i#*=}"
      shift
      ;;
    --join=*)
      join="${i#*=}"
      shift
      ;;
    --secure)
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

fn_print_info "name           = ${name}"
fn_print_info "locality       = ${locality}"
fn_print_info "listen_addr    = ${listen_addr}"
fn_print_info "advertise_addr = ${advertise_addr}"
fn_print_info "sql_addr       = ${sql_addr}"
fn_print_info "http_addr      = ${http_addr}"
fn_print_info "join           = ${join}"

if [ -z "${name}" ]; then
  fn_print_error "Missing name parameter!"
  exit 1
fi

if [ -z "${locality}" ]; then
  fn_print_error "Missing locality parameter!"
  exit 1
fi

if [ -z "${join}" ]; then
  fn_print_error "Missing join parameter!"
  exit 1
fi

#
# Begin script
#

mempool="10%"

fn_local_node_status $listen_addr

if [ "${status}" != "0" ]; then
  fn_print_warn "Node with listen address ${listen_addr} is already running!"
  exit 0
fi

fn_assert_binaries

fn_print_dots "Starting node [--sql-addr=${sql_addr} --locality=${locality}] in $security_mode mode"

case "$security_mode" in
  secure)
    fn_fail_check ${installdir}/cockroach start \
    --locality=${locality} \
    --listen-addr=${listen_addr} \
    --advertise-addr=${advertise_addr} \
    --sql-addr=${sql_addr} \
    --http-addr=${http_addr} \
    --join=${join} \
    --store=${datadir}/${name} \
    --cache=${mempool} \
    --max-sql-memory=${mempool} \
    --background \
    --accept-sql-without-tls \
    --certs-dir=${certsdir}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach start \
    --locality=${locality} \
    --listen-addr=${listen_addr} \
    --advertise-addr=${advertise_addr} \
    --sql-addr=${sql_addr} \
    --http-addr=${http_addr} \
    --join=${join} \
    --store=${datadir}/${name} \
    --cache=${mempool} \
    --max-sql-memory=${mempool} \
    --background \
    --insecure
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

fn_print_ok "Started node successfully"
