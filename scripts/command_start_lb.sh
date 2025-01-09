#!/bin/bash

fn_assert_binaries

case "$security_mode" in
  secure)
    configfile=${configdir}/haproxy-secure.cfg
    ;;
  insecure)
    configfile=${configdir}/haproxy.cfg
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

if [ ! -f ${configfile} ]; then
  fn_print_info "No '${configfile}' found - generating it"

  case "$security_mode" in
    secure)
      fn_fail_check ${installdir}/cockroach gen haproxy --certs-dir=${certsdir} --host=${host}:${rpcportbase}
      ;;
    insecure)
      fn_fail_check ${installdir}/cockroach gen haproxy --insecure --host=${host}:${rpcportbase}
      ;;
    *)
      echo "Bad security mode: $security_mode"
      exit 1
  esac

  fn_fail_check mv ${rootdir}/haproxy.cfg ${configfile}
fi

if [ -f .haproxy.pid ]; then
   fn_print_warn ".haproxy.pid found - already running?"
   exit 1
fi

fn_fail_check haproxy -D -f ${configfile} -p .haproxy.pid

fn_print_ok "Started haproxy (pid: $(<.haproxy.pid))"