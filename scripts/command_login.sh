#!/bin/bash

commandaction="Login"

expire_after="2h30m"

for i in "$@"; do
  case $i in
    --user-name=*)
      user_name="${i#*=}"
      shift
      ;;
    --expire-after=*)
      expire_after="${i#*=}"
      shift
      ;;
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

fn_print_info "user_name    = ${user_name}"
fn_print_info "expire_after = ${expire_after}"
fn_print_info "url          = ${url}"

if [ -z "${user_name}" ]; then
  fn_print_error "Missing user-name parameter!"
  exit 1
fi
if [ -z "${url}" ]; then
  fn_print_error "Missing url parameter!"
  exit 1
fi

fn_fail_check ${installdir}/cockroach auth-session login ${user_name} --expire-after=${expire_after} --only-cookie -url ${url}

exit 0