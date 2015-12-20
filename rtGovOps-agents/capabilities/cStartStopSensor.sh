#!/bin/bash

BASE="/usr/lib/cgi-bin/capabilities"

log(){
  echo "Sensor start/stop Manager: " $1 >> $BASE/log 2>&1
}

if [ -z $1 ]
then
  log "No method provided!"
  exit 0
elif [ -n $1 ]
then
  method=$1
fi

WORK_DIR="/tmp/sensor"
case $method in
                        "start") log "Start sensor!"
								(
								cd $WORK_DIR
								 ./container_run_bg.sh
								#echo $! > $WORK_DIR/sensor.pid
								)
								echo "Sensor started sucessfully!"
								echo PID=; cat $WORK_DIR/sensor.pid
								;;
                        "stop") log "Stop sensor!";
								kill `cat $WORK_DIR/sensor.pid`
								echo "Sensor stopped sucessfully!"
								;;
						*)      log "Unsupported method! $method";;
esac

exit 0;
