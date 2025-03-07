#!/bin/bash

if ! fn_prompt_yes_no "Generate security certificates to ${certsdir}?" Y; then
  exit 0
fi

mkdir -p ${certsdir}

# Create CA cert and PKS12 keystore on demand
if [ ! -f ${certsdir}/ca.key ]; then
  fn_print_info "Creating new CA cert"
  fn_fail_check ${installdir}/cockroach cert create-ca --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key
fi

if [ ! -f ${certsdir}/pestcontrol.p12 ]; then
  fn_print_info "Creating new PKS12 truststore for CA cert"
  keytool -import -noprompt -alias pestcontrol -storepass cockroach -keystore ${certsdir}/pestcontrol.p12 -file ${certsdir}/ca.crt
fi

# Shared node cert
fn_fail_check ${installdir}/cockroach cert create-node localhost $(hostname) --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key

# Create both root and configured user client certs
fn_fail_check ${installdir}/cockroach cert create-client root --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key
fn_fail_check ${installdir}/cockroach cert create-client ${db_username} --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key
fn_fail_check ${installdir}/cockroach cert list --certs-dir=${certsdir}

fn_print_ok "Done"