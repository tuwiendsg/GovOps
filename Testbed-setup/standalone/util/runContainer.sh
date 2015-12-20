#!/bin/bash

if [ -n "$1" ] && [ -n "$2" ] 
then
  PORT=$1
  GWTYPE=$2

else
  echo "Port and gateway type required"
  exit 1
fi

sudo docker run -e SALSA_ENV_PORTMAP_80=`~/myIP.sh`:$PORT -d  -p `~/myIP.sh`:$PORT:80 -t gateway/$GWTYPE
