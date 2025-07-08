#!/bin/bash

fn_assert_binaries() {
  if [ ! -f ${installdir}/cockroach ]; then
     fn_print_error "No cockroach binary found! Run ./pest-control install"
     exit 1
  fi
}

fn_assert_proxies() {
  if [ "${toxiproxy}" != "off" ]; then
    pid=$(ps -ef | grep "toxiproxy-server" | grep "host" | awk '{print $2}')
    if [ -x ${pid} ]; then
       fn_print_error "toxiproxy mode is on but no toxiproxy-server appears to be running. You need to run 'start-toxi' before starting any nodes."
       exit 1
    fi
  fi
}

fn_local_node_status() {
  listen_addr=$1

  if [ $# -eq 0 ]; then
      echo -e "Expected address"
      exit 1
  fi

  case "$OSTYPE" in
    darwin*)
        roachpid=$(lsof -PiTCP -sTCP:LISTEN | grep LISTEN | grep $listen_addr | grep cockroach |  awk '{ print $2 }')
        ;;
    *)
        roachpid=$(netstat -nap 2>/dev/null | grep LISTEN | grep $listen_addr | grep cockroach | awk '{ print $6 }' | awk -F'/' '{ print $1 }')
        ;;
  esac

  if [ -z "${roachpid}" ]; then
      status=0
  else
      status=1
  fi
}

fn_local_stop() {
  pid=$1

  if [ $# -eq 0 ]; then
      echo -e "Expected pid"
      exit 1
  fi

  kill -TERM ${pid}

  fn_print_dots "Waiting for server to stop (pid: $pid)"

  let attempts=0
  while kill -0 $pid 2>/dev/null; do
      printf '.'
      sleep 2
      let attempts=attempts+1
      if [ ${attempts} -gt 5 ]; then
        fn_print_warn "Giving up waiting (${attempts}) - issuing SIGKILL (pid: $pid)"
        kill -KILL ${pid}
        break
      fi
  done

  fn_print_ok "Stopped (pid: $pid)"
}

fn_local_kill() {
  pid=$1

  if [ $# -eq 0 ]; then
      echo -e "Expected pid"
      exit 1
  fi

  kill -KILL ${pid}

  fn_print_dots "Waiting for server to stop (pid: $pid)"

  while kill -0 $pid 2>/dev/null; do
      printf '.'
      sleep 1
  done

  fn_print_ok "Killed (pid: $pid)"
}
