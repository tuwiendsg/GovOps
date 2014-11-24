#!/bin/sh

alias echo='echo -e \\t'

APP_HOME="/data/G2021/G2021Apps"
mkdir -p $APP_HOME/Modbus
cp g2021-modbus/artifacts/G2021Modbus.sab $APP_HOME/Modbus/G2021Modbus.sab
cp g2021-modbus/artifacts/G2021Modbus.sax $APP_HOME/Modbus/G2021Modbus.sax
cp g2021-modbus/artifacts/Kits.scode $APP_HOME/Modbus/Kits.scode
cp g2021-modbus/artifacts/Kits.xml $APP_HOME/Modbus/Kits.xml

chmod +x $APP_HOME/Modbus

if [ $? -ne 0 ]
then
  echo Error installing application!
  exit 1
fi

sed -i '/APP_HOME/d' ~/.profile
echo export APP_HOME=$APP_HOME >> ~/.profile

exit 0
