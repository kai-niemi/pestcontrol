#!/bin/bash

commandaction="Generate node certificates"

for i in "$@"; do
  case $i in
    --name=*)
      name="${i#*=}"
      shift
      ;;
    -*|--*)
      fn_print_warn "Unknown option $i"
      ;;
    *)
      ;;
  esac
done

host_names=("$*")

fn_print_info "name       = ${name}"
fn_print_info "host_names = ${host_names}"

if [ -z "${name}" ]; then
  fn_print_error "Missing name parameter!"
  exit 1
fi
if [ -z "${host_names}" ]; then
  fn_print_error "Missing host names!"
  exit 1
fi

fn_fail_check ${installdir}/cockroach cert create-node ${host_names} --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key

# Prefix key pairs

mkdir -p ${certsdir}/${name}
cp ${certsdir}/node.crt ${certsdir}/${name}
cp ${certsdir}/node.key ${certsdir}/${name}

# List

fn_fail_check ${installdir}/cockroach cert list --certs-dir=${certsdir}
fn_fail_check ${installdir}/cockroach cert list --certs-dir=${certsdir}/${name}

exit 0