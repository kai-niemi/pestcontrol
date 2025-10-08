#!/bin/bash

# ./pop start-haproxy --advertise-addr=localhost:26257

commandaction="Generate haproxy cfg"

security_mode="insecure"
configfile=${datadir}/haproxy.cfg

for i in "$@"; do
  case $i in
    --rpc-addr=*)
      rpc_addr="${i#*=}"
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
  fn_print_warn "Missing rpc_addr parameter!"
  exit 0
fi

fn_print_info "rpc_addr       = ${rpc_addr}"
fn_print_info "security_mode  = ${security_mode}"

fn_assert_binaries

case "$security_mode" in
  secure)
    fn_fail_check ${installdir}/cockroach gen haproxy --certs-dir=${certsdir} --host=${rpc_addr} --out=${configfile}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach gen haproxy --insecure --host=${rpc_addr} --out=${configfile}
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

echo "listen stats" >> ${configfile}
echo "    bind :7070" >> ${configfile}
echo "    mode http" >> ${configfile}
echo "    stats enable" >> ${configfile}
echo "    stats hide-version" >> ${configfile}
echo "    stats realm Haproxy\ Statistics" >> ${configfile}
echo "    stats uri /" >> ${configfile}

cat ${configfile}

exit 0
