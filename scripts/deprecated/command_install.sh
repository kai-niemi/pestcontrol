#!/bin/bash

case "$OSTYPE" in
  darwin*)
    release_version=${version_darwin}
    ;;
  *)
    release_version=${version_linux}
    ;;
esac

if ! fn_prompt_yes_no "Download and install ${release_version} to ${installdir}?" Y; then
  exit 0
fi

mkdir -p ${installdir}

cd ${installdir} || exit

case "$OSTYPE" in
  darwin*)
    curl https://binaries.cockroachdb.com/cockroach-${release_version}.tgz | tar -xz; cp -i cockroach-${release_version}/cockroach ${installdir}
    ;;
  *)
    wget https://binaries.cockroachdb.com/cockroach-${release_version}.tgz; tar -xvf cockroach-${release_version}.tgz; cp -i cockroach-${release_version}/cockroach ${installdir}
    ;;
esac

