#!/bin/bash

commandaction="Generate CA certificate and key pairs"

mkdir -p ${certsdir}

# Create CA cert and PKS12 keystore on demand
if [ ! -f ${certsdir}/ca.key ]; then
  fn_print_info "Creating new CA cert"
  fn_fail_check ${installdir}/cockroach cert create-ca --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key
fi

if [ ! -f ${certsdir}/pestcontrol.p12 ]; then
  fn_print_info "Creating new PKCS12 truststore for CA cert"
  keytool -import -noprompt -alias pestcontrol -storepass cockroach -keystore ${certsdir}/pestcontrol.p12 -file ${certsdir}/ca.crt
fi

rm -fv ${certsdir}/node.crt
rm -fv ${certsdir}/node.key

# Create user client certs
fn_fail_check ${installdir}/cockroach cert create-client root --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key
fn_fail_check ${installdir}/cockroach cert create-client craig --overwrite --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key

# List
fn_fail_check ${installdir}/cockroach cert list --certs-dir=${certsdir}

fn_print_ok "Done"