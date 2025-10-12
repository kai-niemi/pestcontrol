#!/bin/bash

pidfile=${datadir}/.haproxy.pid

if [ ! -f ${pidfile} ]; then
   fn_print_error "No pid found - is it running?"
   exit 1
fi

kill -TERM `cat ${pidfile}`
RETVAL=$?

rm -f ${pidfile}

fn_print_ok "Stopped haproxy (pid: $(<.haproxy.pid))"
