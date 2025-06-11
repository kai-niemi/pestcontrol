#!/bin/bash

commandaction="Stop node"

fn_local_select_pid ${host}:$1

if [ -z $pid ]; then
  fn_print_info "No cockroachdb process found"
  exit 0
fi

fn_local_stop "$pid"

