#!/bin/bash

commandaction="Query node status"

security_mode="insecure"
format="json"
node="--all"

for i in "$@"; do
  case $i in
    --url=*)
      url="${i#*=}"
      shift
      ;;
    --node=*)
      node="${i#*=}"
      shift
      ;;
    --format=*)
      format="${i#*=}"
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

if [ -z "${url}" ]; then
  fn_print_error "Missing url parameter!"
  exit 1
fi

fn_assert_binaries

case "$security_mode" in
  secure)
    fn_fail_check ${installdir}/cockroach node status ${node} --url ${url} \
    --certs-dir=${certsdir} \
    --format ${format}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach node status ${node} --url ${url} \
    --insecure \
    --format ${format}
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

exit 0