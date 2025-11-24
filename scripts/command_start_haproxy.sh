#!/bin/bash

commandaction="Start haproxy"

configfile=${configdir}/haproxy.cfg
pidfile=${datadir}/.haproxy.pid

fn_assert_binaries

if [ ! -f ${configfile} ]; then
  fn_print_info "No '${configfile}' file found!"
else
  fn_print_info "Using '${configfile}' file!"
fi

if [ -f ${pidfile} ]; then
   fn_print_warn "pid found - already running?"
   exit 1
fi

fn_fail_check haproxy -D -f ${configfile} -p ${pidfile}

fn_print_ok "Started haproxy (pid: $(<${pidfile}))"
