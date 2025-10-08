#!/bin/bash

commandaction="Start SQL shell"

# https://www.cockroachlabs.com/docs/stable/cockroach-sql#flags

security_mode="insecure"
db_username=craig

for i in "$@"; do
  case $i in
    --sql-addr=*)
      sql_addr="${i#*=}"
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

if [ -z "${sql_addr}" ]; then
  fn_print_error "Missing sql_addr parameter!"
  exit 1
fi

fn_print_info "sql_addr       = ${sql_addr}"
fn_print_info "security_mode  = ${security_mode}"
fn_print_info "db_username    = ${db_username}"

#
# Begin script
#

fn_assert_binaries

fn_print_dots "Starting SQL shell"

case "$security_mode" in
  secure)
    fn_fail_check ${installdir}/cockroach sql \
    --host=${sql_addr} --user=${db_username} \
    --certs-dir=${certsdir}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach sql \
      --host=${sql_addr} --user=${db_username} \
      --insecure
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

exit 0