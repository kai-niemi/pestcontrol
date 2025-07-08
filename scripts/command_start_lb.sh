#!/bin/bash

# ./pest-control start-lb --advertise-addr=localhost:26257

commandaction="Start haproxy"

security_mode="insecure"
configfile=${datadir}/haproxy.cfg

for i in "$@"; do
  case $i in
    --advertise-addr=*)
      advertise_addr="${i#*=}"
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

if [ -z "${advertise_addr}" ]; then
  fn_print_warn "Missing advertise_addr parameter - using default!"
  advertise_addr="localhost:25257"
fi

fn_print_info "advertise_addr = ${advertise_addr}"
fn_print_info "security_mode  = ${security_mode}"

fn_assert_binaries

if [ ! -f ${configfile} ]; then
  case "$security_mode" in
    secure)
      fn_fail_check ${installdir}/cockroach gen haproxy --certs-dir=${certsdir} --host=${advertise_addr}
      ;;
    insecure)
      fn_fail_check ${installdir}/cockroach gen haproxy --insecure --host=${advertise_addr}
      ;;
    *)
      echo "Bad security mode: $security_mode"
      exit 1
  esac

  echo "listen stats" >> ${rootdir}/haproxy.cfg
  echo "    bind :7070" >> ${rootdir}/haproxy.cfg
  echo "    mode http" >> ${rootdir}/haproxy.cfg
  echo "    stats enable" >> ${rootdir}/haproxy.cfg
  echo "    stats hide-version" >> ${rootdir}/haproxy.cfg
  echo "    stats realm Haproxy\ Statistics" >> ${rootdir}/haproxy.cfg
  echo "    stats uri /" >> ${rootdir}/haproxy.cfg

  mv ${rootdir}/haproxy.cfg ${configfile}

  fn_print_info "No '${configfile}' file found - generated it"
else
  fn_print_info "Using existing '${configfile}' file (delete to re-generate)"
fi

if [ -f .haproxy.pid ]; then
   fn_print_warn ".haproxy.pid found - already running?"
   exit 1
fi

fn_fail_check haproxy -D -f ${configfile} -p .haproxy.pid

fn_print_ok "Started haproxy (pid: $(<.haproxy.pid))"

open "http://localhost:7070"