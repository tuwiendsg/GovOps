#!/bin/sh                                                                       
alias echo='echo -e'
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

#TODO redirect my output to log file. this can then be configured
echo ">>>>>>>>>>>>>>>>>>"
echo "Start new update check on `date`."

PROC_NUM=$(ps | grep {agent.sh} | grep -v "grep" | sed -n '1p'|awk '{print $1}')

if [ $PROC_NUM -ne $$ ]
  then
	finishError "Cannot run agent. An instance with PID=$PROC_NUM is already running!"
fi	

MAC=$(/sbin/ifconfig eth0 | grep -o -E '([[:xdigit:]]{1,2}:){5}[[:xdigit:]]{1,2}')
echo "My MAC address is $MAC"

SERVER="http://128.130.172.231"
BASEURL="$SERVER:8080/SDGManager/device-manager"

#Send device profile to the manager.
PROFILE_URL="$BASEURL/profile/$MAC"
echo Send profile to Manager at: $PROFILE_URL

./profile.sh > tmp
wget -O - $PROFILE_URL --post-data "`cat tmp`"
rm tmp
echo Profile successfully sent to Manager!
echo

#Check if there is update available and download it.
UPDATE_URL="$BASEURL/update/$MAC"
echo "Fetch update from $UPDATE_URL"

SERVER_RESPONSE=$(wget -O response.zip $UPDATE_URL 2>&1)
#hm --sever-response is not working

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

mkdir tmp/
unzip response.zip -d tmp/

if [ $? -ne 0 ]
  then                                               
    echo "Server says: `cat response.zip`" 
	rm -rf tmp/
	rm response.zip
    finishSuccess                                                
  else echo "Unzip successful!"
fi

echo
echo "Try to install the update ..."

ID=`cat tmp/id`
echo "Starting run script from image:$ID"
echo "--------------------"

(
cd tmp
sh run.sh
)

echo "--------------------"
echo "Finished execution!!"
echo

rm -rf tmp/
rm response.zip

UPDATE_DONE_URL="$BASEURL/update-successful/$MAC/$ID"
echo "Notify Manager at: $UPDATE_DONE_URL"

SERVER_RESPONSE=$(wget -O response $UPDATE_DONE_URL 2>&1)

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
  echo "Manager says: `cat response`"
fi

rm response
finishSuccess



