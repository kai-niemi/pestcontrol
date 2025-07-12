#!/bin/bash

case "$OSTYPE" in
  darwin*)
        default="\x1B[0m"
        red="\x1B[31m"
        green="\x1B[32m"
        lightyellow="\x1B[93m"
        lightblue="\x1B[94m"
        cyan="\x1B[36m"
        lightcyan="\x1B[96m"
        creeol="\r\033[K"
        ;;
  *)
        default="\e[0m"
        red="\e[31m"
        green="\e[32m"
        lightyellow="\e[93m"
        lightblue="\e[94m"
        cyan="\e[36m"
        lightcyan="\e[96m"
        creeol="\r\033[K"
        ;;
esac

fn_sleep_time(){
  sleep 0.5
}

fn_echo_info_nl(){
  if [ "${commandaction}" ]; then
    echo -en "${creeol}[${cyan} INFO ${default}] ${commandaction}: $*"
  else
    echo -en "${creeol}[${cyan} INFO ${default}] $*"
  fi
	fn_sleep_time
	echo -en "\n"
}

fn_echo_fail_nl(){
  if [ "${commandaction}" ]; then
    echo -en "${creeol}[${cyan} FAIL ${default}] ${commandaction}: $*"
  else
    echo -en "${creeol}[${cyan} FAIL ${default}] $*"
  fi
	fn_sleep_time
	echo -en "\n"
}

fn_echo_warn_nl(){
  if [ "${commandaction}" ]; then
    echo -en "${creeol}[${lightyellow} WARN ${default}] ${commandaction}: $*"
  else
    echo -en "${creeol}[${lightyellow} WARN ${default}] $*"
  fi
	fn_sleep_time
	echo -en "\n"
}

fn_echo_dryrun_nl(){
  echo -en "[DRYRUN] $*"
	echo -en "\n"
}

fn_echo_cmd_nl(){
  echo -en "[COMMAND] $*"
	echo -en "\n"
}

fn_echo_header(){
	echo -e ""
	echo -e "${lightyellow}${title} ${default}"
	echo -e "==========================================${default}"
}

fn_prompt_yes_no(){
	local prompt="${cyan}$1${default}"

	while true; do
	  echo -e "${prompt}"
		select yn in "Yes" "Skip" "Quit"; do
        case $yn in
            Yes ) return 0 ;;
            Skip ) return 1 ;;
            Quit ) exit 0 ;;
        *) echo -e "Please answer yes, skip or quit." ;;
        esac
	  done
	done
}

fn_failcheck(){
    if [ "${dryrun}" == "on" ]; then
      fn_echo_dryrun_nl "$@"
    else
      fn_echo_cmd_nl "$@"
      "$@"
      local status=$?
      if [ ${status} -ne 0 ]; then
          fn_echo_fail_nl "$@" >&2
          exit 1
      fi
      return ${status}
    fi
}

fn_open_url(){
  case "$OSTYPE" in
  darwin*)
        open "$@"
        ;;
  linux*)
        if [ -n $BROWSER ]; then
          $BROWSER "$@"
        else
          fn_echo_fail_nl "Could not detect web browser to use."
        fi
        ;;
  *)
        fn_echo_fail_nl "Unknown OS: $OSTYPE"
        exit 1
        ;;
  esac
}

fn_split_array() {
  local IN="$1"
  OUT=$(echo $IN | awk -v RS='[[:blank:]]|[\n]|[,]' '{max=a[split($0, a ,"-")]; if(max!=0){while(a[1]<=max){print a[1]++}}}')
}

