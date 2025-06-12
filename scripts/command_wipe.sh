#!/bin/bash

fn_local_kill_all

if fn_prompt_yes_no "Delete '${certsdir}' - are you sure?" Y; then
  rm -rf ${certsdir}
fi

if fn_prompt_yes_no "Delete '${datadir}' - are you sure?" Y; then
  rm -rf ${datadir}
fi

if fn_prompt_yes_no "Delete '${installdir}' - are you sure?" Y; then
  rm -rf ${installdir}
fi
