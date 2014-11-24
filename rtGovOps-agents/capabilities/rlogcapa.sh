#!/bin/sh

log(){

  echo "Read log capability:" $1 >> /usr/lib/cgi-bin/capabilities/log 2>&1

}

if [ -z $2 ]
then
  log "No log file provided!"
  exit 0
elif [ -n $2 ]
then
  logFile=$2
fi

cat $logFile