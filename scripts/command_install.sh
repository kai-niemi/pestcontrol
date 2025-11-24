#!/bin/bash

commandaction="Install binaries"

for i in "$@"; do
  case $i in
    --version=*)
      version="${i#*=}"
      shift
      ;;
    -*|--*)
      fn_print_warn "Unknown option $i"
      ;;
    *)
      ;;
  esac
done

fn_print_info "version = ${version}"

#
# Begin script
#

mkdir -p ${installdir}

cd ${installdir} || exit

case "$OSTYPE" in
  darwin*)
    curl https://binaries.cockroachdb.com/cockroach-${version}.tgz | tar -xz; cp cockroach-${version}/cockroach ${installdir}
    ;;
  *)
    wget https://binaries.cockroachdb.com/cockroach-${version}.tgz; tar -xvf cockroach-${version}.tgz; cp cockroach-${version}/cockroach ${installdir}
    ;;
esac

exit 0