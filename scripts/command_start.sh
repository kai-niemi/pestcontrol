#!/bin/bash

commandaction="Start node"

# ./pest-operator start --name=n1 --locality=region=eu-north-1,zone=eu-north-1a --listen-addr=localhost:26257 --advertise-addr=localhost:26257 --http-addr=localhost:8080 --join=localhost:25257,localhost:25258,localhost:25259
# ./pest-operator start --name=n2 --locality=region=eu-north-1,zone=eu-north-1b --listen-addr=localhost:26258 --advertise-addr=localhost:26258 --http-addr=localhost:8081 --join=localhost:25257,localhost:25258,localhost:25259
# ./pest-operator start --name=n3 --locality=region=eu-north-1,zone=eu-north-1c --listen-addr=localhost:26259 --advertise-addr=localhost:26259 --http-addr=localhost:8082 --join=localhost:25257,localhost:25258,localhost:25259

# https://www.cockroachlabs.com/docs/stable/cockroach-start#flags

security_mode="insecure"

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
      security_mode="secure"
      shift
      ;;
    -*|--*)
      fn_print_warn "Unknown option $i"
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
fn_print_info "security_mode  = ${security_mode}"

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

fn_assert_binaries

#
# Begin script
#

mempool="10%"

fn_local_node_status $advertise_addr

if [ "${status}" != "0" ]; then
  fn_print_warn "Node with advertise address ${advertise_addr} is already running!"
  exit 0
fi

fn_print_dots "Starting node ${name}"

case "$security_mode" in
  secure)
    fn_fail_check ${installdir}/cockroach start \
    --locality=${locality} \
    ${listen_addr:+--listen-addr=${listen_addr}} \
    ${advertise_addr:+--advertise-addr=${advertise_addr}} \
    ${sql_addr:+--sql-addr=${sql_addr}} \
    ${http_addr:+--http-addr=${http_addr}} \
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
    ${listen_addr:+--listen-addr=${listen_addr}} \
    ${advertise_addr:+--advertise-addr=${advertise_addr}} \
    ${sql_addr:+--sql-addr=${sql_addr}} \
    ${http_addr:+--http-addr=${http_addr}} \
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
