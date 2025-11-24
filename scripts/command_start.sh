!/bin/bash

commandaction="Start node"

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
    --cluster-name=*)
      cluster_name="${i#*=}"
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
fn_print_info "cluster_name   = ${cluster_name}"

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

if [ "${listen_addr}" ]; then
  case "$OSTYPE" in
    darwin*)
        roachpid=$(lsof -PiTCP -sTCP:LISTEN | grep LISTEN | grep $listen_addr | grep cockroach |  awk '{ print $2 }')
        ;;
    *)
        roachpid=$(netstat -nap 2>/dev/null | grep LISTEN | grep $listen_addr | grep cockroach | awk '{ print $6 }' | awk -F'/' '{ print $1 }')
        ;;
  esac

  if [ "${roachpid}" ]; then
     fn_print_warn "Node with --listen-addr ${listen_addr} is already running!"
     exit 0
  fi
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
    --certs-dir=${certsdir} \
    ${cluster_name:+--cluster-name=${cluster_name}}
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
    --insecure \
    ${cluster_name:+--cluster-name=${cluster_name}}
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

exit 0