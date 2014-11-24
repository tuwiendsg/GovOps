#!/bin/sh

alias echo='echo -e \\t'

if env | grep -q ^SVM_HOME= || [[ ! -z $SVM_HOME ]]
then
  echo SVM home is $SVM_HOME
else
  echo SVM_HOME must be set to run Modbus apps!
  exit 1
fi

touch ~/sedona.out
exec $SVM_HOME/svm $APP_HOME/Modbus/Kits.scode $APP_HOME/Modbus/G2021Modbus.sab > ~/sedona.out 2>&1 &

exit 0
