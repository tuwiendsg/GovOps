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
			"list")		log "List capabilities!"
						list=$(ls $BASE |grep -v cManager.sh|grep -wv log)
						echo "\"capabilities\":["
						set -- $list
						for element in "$@"; do
								echo "{\"capability\":\"$element\"},"
						done
						echo "]"
						;;
	"change") log "Change cloud connectivity protocol!";
						if [ -n "$2" -a "$2" != " " ]
						then
							CONFIG_BASE="/usr/share/location-service/config-files/META-INF"
							case $2 in 
								"mqtt") 
									cp $CONFIG_BASE/wire.mqtt.xml $CONFIG_BASE/wire.xml
									touch  $CONFIG_BASE/wire.xml
								/usr/lib/cgi-bin/capabilities/cService.sh "stop"
								/usr/lib/cgi-bin/capabilities/cService.sh start
									echo "Protocol set to mqtt!"
								;;
								"coap")
									cp $CONFIG_BASE/wire.coap.xml $CONFIG_BASE/wire.xml 
									touch  $CONFIG_BASE/wire.xml
								/usr/lib/cgi-bin/capabilities/cService.sh "stop"
								/usr/lib/cgi-bin/capabilities/cService.sh start
									echo "Protocol set to coap!"
								;;							
							esac
						else
							log "No protocol name provided!"
							echo "No protocol name provided!"
						fi
                        ;;
					*)      log "Unsupported method! $method";;
esac

exit 0;
