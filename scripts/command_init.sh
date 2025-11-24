#!/bin/bash

commandaction="Initialize cluster"

# https://www.cockroachlabs.com/docs/stable/cockroach-init#flags

security_mode="insecure"
db_username=craig
db_password=cockroach

for i in "$@"; do
  case $i in
    --rpc-addr=*)
      rpc_addr="${i#*=}"
      shift
      ;;
    --sql-addr=*)
      sql_addr="${i#*=}"
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

if [ -z "${rpc_addr}" ]; then
  fn_print_error "Missing rpc_addr parameter!"
  exit 1
fi

if [ -z "${sql_addr}" ]; then
  fn_print_warn "Missing sql_addr parameter !"
  exit 1
fi

fn_print_info "rpc_addr       = ${rpc_addr}"
fn_print_info "sql_addr       = ${sql_addr}"
fn_print_info "security_mode  = ${security_mode}"
fn_print_info "cluster_name   = ${cluster_name}"
fn_print_info "db_username    = ${db_username}"
fn_print_info "db_password    = ******"

#
# Begin script
#

fn_assert_binaries

case "$security_mode" in
  secure)
    ${installdir}/cockroach init --certs-dir=${certsdir} --host=${rpc_addr} \
        ${cluster_name:+--cluster-name=${cluster_name}}

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${sql_addr} \
    -e "CREATE USER IF NOT EXISTS ${db_username} WITH PASSWORD '${db_password}'"

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${sql_addr} \
    -e "ALTER ROLE ${db_username} WITH PASSWORD '${db_password}'; GRANT ADMIN to ${db_username};"

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${sql_addr} < ${configdir}/init.sql

    if [ -f ${configdir}/init-dev.sql ]; then
      ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${sql_addr} < ${configdir}/init-dev.sql
    fi

    ;;
  insecure)
    ${installdir}/cockroach init --insecure --host=${rpc_addr} \
        ${cluster_name:+--cluster-name=${cluster_name}}

    ${installdir}/cockroach sql --insecure --host=${sql_addr} \
    -e "CREATE USER IF NOT EXISTS ${db_username}; GRANT ADMIN to ${db_username};"

    ${installdir}/cockroach sql --insecure --host=${sql_addr} \
    -e "ALTER ROLE ${db_username} WITH PASSWORD NULL"

    ${installdir}/cockroach sql --insecure --host=${sql_addr} < ${configdir}/init.sql

    if [ -f ${configdir}/init-dev.sql ]; then
      ${installdir}/cockroach sql --insecure --host=${sql_addr} < ${configdir}/init-dev.sql
    fi

    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

exit 0