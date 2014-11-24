#!/bin/sh

alias echo='echo -e \\t'

echo Remove old installation at $APP_HOME/Modbus
rm -r $APP_HOME/Modbus

exit 0
