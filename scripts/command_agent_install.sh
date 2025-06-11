#!/bin/bash

# ./cluster-admin agent-install --version=v25.1.3.darwin-11.0-arm64

commandaction="Install binaries"

for i in "$@"; do
  case $i in
    --version=*)
      version="${i#*=}"
      shift
      ;;
    -*|--*)
      echo "Unknown option $i"
      exit 1
      ;;
    *)
      ;;
  esac
done

if [ -z "${version}" ]; then
  case "$OSTYPE" in
    darwin*)
      release_version=${version_darwin}
      ;;
    *)
      release_version=${version_linux}
      ;;
  esac

  version=${release_version}

  fn_print_warn "Missing version parameter - using default!"
fi

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