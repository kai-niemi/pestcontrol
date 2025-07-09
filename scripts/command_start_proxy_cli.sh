#!/bin/bash

# ./pest-operator start-proxy-cli --listen-addr=localhost:36257 --upstream-addr=localhost:26257 --name=n1
# ./pest-operator start-proxy-cli --listen-addr=localhost:36258 --upstream-addr=localhost:26258 --name=n2
# ./pest-operator start-proxy-cli --listen-addr=localhost:36259 --upstream-addr=localhost:26259 --name=n3

commandaction="Start toxiproxy client"

# Toxiproxy server host
toxiproxy_host=localhost
# Toxiproxy server port
toxiproxy_port=8474

for i in "$@"; do
  case $i in
    --toxiproxy-host=*)
      toxiproxy_host="${i#*=}"
      shift
      ;;
    --toxiproxy-port=*)
      toxiproxy_port="${i#*=}"
      shift
      ;;
    --listen-addr=*)
      listen_addr="${i#*=}"
      shift
      ;;
    --upstream-addr=*)
      upstream_addr="${i#*=}"
      shift
      ;;
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

fn_print_info "toxiproxy_host  = ${toxiproxy_host}"
fn_print_info "toxiproxy_port  = ${toxiproxy_port}"
fn_print_info "listen_addr     = ${listen_addr}"
fn_print_info "upstream_addr   = ${upstream_addr}"
fn_print_info "name            = ${name}"

if [ -z "${toxiproxy_host}" ]; then
  fn_print_error "Missing toxiproxy-host parameter!"
  exit 1
fi
if [ -z "${toxiproxy_port}" ]; then
  fn_print_error "Missing toxiproxy-port parameter!"
  exit 1
fi
if [ -z "${listen_addr}" ]; then
  fn_print_error "Missing listen-addr parameter!"
  exit 1
fi
if [ -z "${upstream_addr}" ]; then
  fn_print_error "Missing upstream-addr parameter!"
  exit 1
fi
if [ -z "${name}" ]; then
  fn_print_error "Missing name parameter!"
  exit 1
fi

fn_fail_check toxiproxy-cli --host ${toxiproxy_host}:${toxiproxy_port} create --listen ${listen_addr} --upstream ${upstream_addr} ${name}
fn_fail_check toxiproxy-cli --host ${toxiproxy_host}:${toxiproxy_port} list

fn_print_ok "Started toxiproxy client (${name})"
