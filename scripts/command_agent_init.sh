#!/bin/bash

# ./cluster-admin agent-init --listen-addr=localhost:25258 --sql-addr=localhost:26258

commandaction="Initialize cluster (agent)"

# https://www.cockroachlabs.com/docs/stable/cockroach-init#flags

for i in "$@"; do
  case $i in
    --listen-addr=*)
      listen_addr="${i#*=}"
      shift
      ;;
    --sql-addr=*)
      sql_addr="${i#*=}"
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

if [ -z "${listen_addr}" ]; then
  fn_print_error "Missing listen_addr parameter!"
  exit 1
fi

if [ -z "${sql_addr}" ]; then
  fn_print_error "Missing sql_addr parameter!"
  exit 1
fi

fn_print_info "listen_addr    = ${listen_addr}"
fn_print_info "sql_addr       = ${sql_addr}"

#
# Begin script
#

fn_assert_binaries

fn_print_dots "Initialize node [--sql-addr=${sql_addr} --locality=${locality}] in $security_mode mode"

case "$security_mode" in
  secure)
    ${installdir}/cockroach init --certs-dir=${certsdir} --host=${listen_addr}

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
    ${installdir}/cockroach init --insecure --host=${listen_addr}

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

fn_print_ok "Cluster initialized"
