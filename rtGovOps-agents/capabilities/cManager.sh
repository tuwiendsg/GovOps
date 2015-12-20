#!/bin/sh

BASE="/usr/lib/cgi-bin/capabilities"

log(){


  echo "Capability Manager: " $1 >> $BASE/log 2>&1

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
	"capabilities") log "Get capability! $2";
						if [ -n "$2" -a "$2" != " " ]
						then
							cName=$(ls $BASE|grep $2)
							if [ -n "$cName" ]
							then
								#echo "{\"result\": {\"capabilityName\":\"$cName\"}"
								echo "{\"id\":\"`uname -n`\", \"capaName\":\"cManager\", \"capaResult\":\"$cName\"}"
								log "Found capability: " $cName
								else
									#echo "{\"capabilityName\":\"NULL\"}"
									echo "{\"id\":\"`uname -n`\", \"capaName\":\"cManager\", \"capaResult\":\"not found\"}"
								fi
							else
								log "No capability name provided!"
						fi
                        ;;
					*)      log "Unsupported method! $method";;
esac

exit 0;
