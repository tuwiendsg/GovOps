#!/bin/sh

AGENT_WORKING_DIR="/tmp/agent"
echo "Agent working dir is: $AGENT_WORKING_DIR"

echo First refresh the environment!
if  [ -f $AGENT_WORKING_DIR/.profile ]
then
  echo .profile exists!
  source $AGENT_WORKING_DIR/.profile
else
  echo No .profile file found! Create one!
  touch $AGENT_WORKING_DIR/.profile
fi
echo
echo Execute the runlist!
echo ==============================
while read component; do
  chmod +x -R $component
  echo ----------$component--------------  
  echo Trying to stop: $component
  ./$component/scripts/stop.sh
  if [ $? -ne 0 ]
  then
    echo Error stopping the component $component!
	exit 0
  fi
  echo Sucessfuly stopped the component $component!
  echo
  
  echo Trying to uninstall the component $component!
  ./$component/scripts/uninstall.sh
  if [ $? -ne 0 ]
  then
    echo Error uninstalling the component $component!
	exit 0
  fi
  source $AGENT_WORKING_DIR/.profile
  echo Succesfully uninstalled the component $component!
  echo
  
  echo Trying to install: $component
  ./$component/scripts/install.sh
  if [ $? -ne 0 ]
  then
    echo Error installing the component $component!
	exit 0
  fi
  source $AGENT_WORKING_DIR/.profile
  echo Sucessfuly installed the component $component!
  echo
  
  echo Trying to start: $component
  ./$component/scripts/run.sh
  if [ $? -ne 0 ]
  then
    echo Error starting the component $component!
	exit 0
  fi
  echo Sucessfuly started the component $component!
  echo
done <runlist
echo ====================================
echo

echo Runlist finished! Buy, buy ...
echo
exit 0