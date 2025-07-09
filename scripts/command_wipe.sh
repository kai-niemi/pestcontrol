#!/bin/bash

fn_print_info ${installdir}
fn_print_info ${certsdir}
fn_print_info ${datadir}

rm -rf ${installdir}
rm -rf ${certsdir}
rm -rfv ${datadir}

fn_print_ok "Wiped local directories"