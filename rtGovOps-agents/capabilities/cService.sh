#!/bin/sh

BASE="/usr/lib/cgi-bin/capabilities"

log(){
  echo "Protocol Manager: " $1 >> $BASE/log 2>&1
}


if [ -z $1 ]
then
  log "No method provided!"
  exit 0
elif [ -n $1 ]
then
  method=$1
fi

case $method in
  "start")  log "Start locaton service!"
            /usr/bin/java -cp "/usr/share/location-service/*" container.Main &
            echo "Sucessfully started the service!"
            ;;
  "stop")  log "Stop location service!"
           JPID=$(ps aux |grep java| grep -v grep | awk '{print $2}')
           if  [ -z $JPID ]
           then
              echo "No service running!"
           else
             /bin/kill $JPID
             echo "Service killed!"
           fi
           ;;

esac

exit 0
