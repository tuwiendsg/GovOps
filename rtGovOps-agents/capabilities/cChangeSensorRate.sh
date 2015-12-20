
#!/bin/sh

BASE="/usr/lib/cgi-bin/capabilities"

log(){
  echo "Sensor Manager: " $1 >> $BASE/log 2>&1
}

if [ -z $1 ]
then
  log "No method or argument provided!"
  exit 0
elif [ -n $1 ]
then
  method=$1
fi


case $method in
                        "start")  log "Start sensor - not suported now!"

                        ;;
                        "update") log "Update sensor push rate!";
                                  if [ -n "$2" -a "$2" != " " ]
                                  then
                                    NEW_VAL=$2
                                    CONFIG_BASE="/tmp/sensor/config-files/META-INF"; 
                                    sed -i '/<property name="updateRate" value="[0-9]*"\/>/c\<property name="updateRate" value="'$NEW_VAL'"\/>' $CONFIG_BASE/wire.xml
                                    echo "Update rate set to $2 seconds!"
                                  else
                                          log "No update rate provided!"
                                          echo "No update rate provided!"
                                  fi
                        ;;
                        *)      log "Unsupported method! $method";;
esac

exit 0;
