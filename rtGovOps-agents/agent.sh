#!/bin/bash
alias echo='echo '
finishSuccess(){
  echo "Successfully finished the update on `date`."
  echo "<<<<<<<<<<<<<<<<<<\n"
  echo
  exit 0
}
finishError(){
  echo "Failed to finish the update on `date`. Error message is: $1"
  echo "<<<<<<<<<<<<<<<<<<\n"
  echo
  exit 1
}

echo ">>>>>>>>>>>>>>>>>>"
echo "Start new update check on `date`."

PROC_NUM=$(ps | grep {agent.sh} | grep -v "grep" | sed -n '1p'|awk '{print $1}')
if [ ! -z $PROC_NUM ] && [ $PROC_NUM -ne $$ ]
then
    finishError "Cannot run agent. An instance with PID=$PROC_NUM is already running!"
fi

################# Initial configuration #################
WORKING_DIR="/tmp/agent"
mkdir $WORKING_DIR
#MAC=`echo $SALSA_ENV_PORTMAP_80`
MAC=`cat /etc/environment|grep SALSA_ENV_PORTMAP_80|tr '=' ' '|awk '{print $2}'`
echo "My unique ID is $MAC"
SERVER_IP="$WORKING_DIR/server.ip"
LB_HOST="http://128.130.172.199:8080/SDGBalancer/balancer"
INIT_CALL="$LB_HOST/assign/$MAC"
echo "Load balancer is at: $INIT_CALL"
#########################################################

#Check if we contacted the load balancer before
#FIXME: add fail-over suppor
if [ ! -e $SERVER_IP ]
then
        echo "First run!"
        # Get dedicated server from load balancer
        SERVER_RESPONSE=$(wget -O $SERVER_IP $INIT_CALL 2>&1)
        TMP=$?
        if [ $TMP == 8 ]
                then
                  TMP=`echo $SERVER_RESPONSE | awk 'match($0, /HTTP.*/) {print substr($0, RSTART, RLENGTH)}'`
                  finishError "Contacting load balancer failed! Server response: $TMP"
                elif [ $TMP -ne 0 ]
                then
                  #echo $SERVER_RESPONSE
                  TMP=`echo $SERVER_RESPONSE | awk 'match($0, /wget:.*/) {print substr($0, RSTART+5, RLENGTH)}'`
                  finishError "Could not connect to load balancer: $SERVER_RESPONSE"
                else
                  echo "My dedicated manager is at: `cat $SERVER_IP`"
        fi
else
        echo "Dedicated Manager is already assigned! Running at: `cat $SERVER_IP` "
fi

#Register gateway
META="location=gh1&type=FM5300"
SERVER=`cat $SERVER_IP`
BASEURL="http://$SERVER:8080/SDGManager/device-manager"
REGISTRATION_URL="$BASEURL/registerMeta/$MAC/$META"
wget -O - $REGISTRATION_URL
echo Registered at host: $REGISTRATION_URL

#Send device profile to the manager.
PROFILE_URL="$BASEURL/profile/$MAC/"
echo Send profile to Manager at: $PROFILE_URL
tmp=$(mktemp)
/usr/share/provi-agent/profile.sh > $tmp
echo "I am a profile! Change DB to give me more space the 255 chars!" >$tmp
wget -O - $PROFILE_URL --post-data "`cat $tmp`"
rm $tmp
echo Profile successfully sent to Manager!
echo


#Check if there is update available and download it.
UPDATE_URL="$BASEURL/update/$MAC/"
echo "Fetch update from $UPDATE_URL"

response_file="$WORKING_DIR/response.zip"
SERVER_RESPONSE=$(wget -O $response_file $UPDATE_URL 2>&1)
TMP=$?
if [ $TMP == 8 ]
then
  TMP=`echo $SERVER_RESPONSE | awk 'match($0, /HTTP.*/) {print substr($0, RSTART, RLENGTH)}'`
  finishError "Download failed! Server response: $TMP"
elif [ $TMP -ne 0 ]
then
  #echo $SERVER_RESPONSE
  TMP=`echo $SERVER_RESPONSE | awk 'match($0, /wget:.*/) {print substr($0, RSTART+5, RLENGTH)}'`
  finishError "Could not connect: $TMP"
else
  echo "Update successfully downloaded!"
fi
echo


echo "Try to unzip the update ..."
tmp_dir="$WORKING_DIR/tmp/"
mkdir $tmp_dir
unzip $response_file -d $tmp_dir

if [ $? -ne 0 ]
  then
    echo "Server says: `cat $response_file`"
        rm -rf $tmp_dir
        rm $response_file
    finishSuccess
  else echo "Unzip successful!"
fi
echo

echo "Try to install the update ..."
ID=`cat $tmp_dir/id`
echo "Starting run script from image:$ID"
echo "--------------------"

(
cd $tmp_dir
sh run.sh
)

echo "--------------------"
echo "Finished execution!!"
echo

rm -rf $tmp_dir
rm $response_file

UPDATE_DONE_URL="$BASEURL/update-successful/$MAC/$ID"
echo "Notify Manager at: $UPDATE_DONE_URL"

response_file="$WORKING_DIR/response"
SERVER_RESPONSE=$(wget -O $response_file $UPDATE_DONE_URL 2>&1)

TMP=$?
if [ $TMP == 8 ]
then
  TMP=`echo $SERVER_RESPONSE | awk 'match($0, /HTTP.*/) {print substr($0, RSTART, RLENGTH)}'`
  finishError "Download failed! Server response: $TMP"
elif [ $TMP -ne 0 ]
then
  #echo $SERVER_RESPONSE
  TMP=`echo $SERVER_RESPONSE | awk 'match($0, /wget:.*/) {print substr($0, RSTART+5, RLENGTH)}'`
  finishError "Could not connect: $TMP"
else
  echo "Manager says: `cat $response_file`"
fi

rm $response_file

echo "Notify balancer about update done!"
DONE_CALL="$LB_HOST/done/$MAC/"
wget -O - $DONE_CALL 2>&1
if [ $? == 0 ]
then
        finishSuccess
else
        #TODO: handle HTTP ACCEPT
        #finishError "Cloud not notify balancer!"
fi

exit 0

