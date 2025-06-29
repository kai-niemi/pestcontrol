#!/bin/bash

commandaction="Generate node certificates"

for i in "$@"; do
  case $i in
    --name=*)
      name="${i#*=}"
      shift
      ;;
    --advertise-addr=*)
      advertise_addr="${i#*=}"
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

fn_print_info "name           = ${name}"
fn_print_info "advertise_addr = ${advertise_addr}"

if [ -z "${name}" ]; then
  fn_print_error "Missing name parameter!"
  exit 1
fi
if [ -z "${advertise_addr}" ]; then
  fn_print_error "Missing advertise_addr parameter!"
  exit 1
fi

fn_fail_check ${installdir}/cockroach cert create-node ${advertise_addr} --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key
# Prefix key pairs
mv ${certsdir}/node.crt ${certsdir}/${name}_node.crt
mv ${certsdir}/node.key ${certsdir}/${name}_node.key

# List
fn_fail_check ${installdir}/cockroach cert list --certs-dir=${certsdir}

fn_print_ok "Done"