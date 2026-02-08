#!/bin/bash

commandaction="Open URL"

for i in "$@"; do
  case $i in
    --url=*)
      url="${i#*=}"
      shift
      ;;
    -*|--*)
      fn_print_warn "Unknown option $i"
      ;;
    *)
      ;;
  esac
done

if [ -z "${url}" ]; then
  fn_print_error "Missing url parameter!"
  exit 1
fi

fn_fail_check open --url ${url}

exit 0