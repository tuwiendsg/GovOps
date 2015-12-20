#!/bin/sh
# /usr/share/cgi-bin/mapper.sh
DEBUG=0

# disable filename globbing
set -f

debug (){
	if [ $DEBUG == 1 ]
	then
		echo $*
	fi
}

echo "Content-type: application/json"
echo

debug PATH_INFO = "$PATH_INFO"
debug "URL format should be /{capabilityId}/{methodName}/{arguments}?arg1={arg1}&arg2={arg2}"

CID=$(echo "$PATH_INFO"| sed s/"\/"/" "/g | awk '{print $1}')
MNAME=$(echo "$PATH_INFO"| sed s/"\/"/" "/g | awk '{print $2}')
ARG=$(echo "$PATH_INFO"| sed s/"\/"/" "/g | awk '{print $3}')

debug CapabilityId = $CID
debug MethodName = $MNAME
debug Arguments = $ARG

if [ -n "$QUERY_STRING" -a -n "$ARG" ]
then
  saveIFS=$IFS
  IFS='&'
  set -- $QUERY_STRING
  IFS=$saveIFS

  debug Argc is: $#
  argv="$@"
  set --

  for argx in $argv; do
   val=$(echo $argx | awk '{split($0,a,"="); print a[2]}')
   set -- $* $val
  done
  debug "ARG= $*"
elif [ ! -z "$ARG" -a "$ARG" != " " ]
then
        #Syntactic sugar for one argument methods
        set --  $ARG
        debug "ARG= $*"
else
  debug "No arguments passed!"
fi

debug
debug Execute the capability: 
ARGS="$@"
#if [ mapingFileAvailable ]
#then
# construct EXEC_C or use components run.sh
#else
#Default capability mapping
EXEC_C="/usr/lib/cgi-bin/capabilities/$CID.sh $MNAME $ARGS"
#fi
debug Execute $EXEC_C
tmp=$(mktemp)
/bin/sh $EXEC_C > $tmp 2>&1
debug Exited with $? and msg:
#Send result as JSON
echo `cat $tmp`
rm $tmp

debug
debug
debug
debug DEBUG INFO:
debug SERVER_SOFTWARE = $SERVER_SOFTWARE
debug SERVER_NAME = $SERVER_NAME
debug GATEWAY_INTERFACE = $GATEWAY_INTERFACE
debug SERVER_PROTOCOL = $SERVER_PROTOCOL
debug SERVER_PORT = $SERVER_PORT
debug REQUEST_METHOD = $REQUEST_METHOD
debug HTTP_ACCEPT = "$HTTP_ACCEPT"
debug PATH_INFO = "$PATH_INFO"
debug PATH_TRANSLATED = "$PATH_TRANSLATED"
debug SCRIPT_NAME = "$SCRIPT_NAME"
debug QUERY_STRING = "$QUERY_STRING"
debug REMOTE_HOST = $REMOTE_HOST
debug REMOTE_ADDR = $REMOTE_ADDR
debug REMOTE_USER = $REMOTE_USER
debug AUTH_TYPE = $AUTH_TYPE
debug CONTENT_TYPE = $CONTENT_TYPE
debug CONTENT_LENGTH = $CONTENT_LENGTH

exit 0