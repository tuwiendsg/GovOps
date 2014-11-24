#!/bin/sh

alias echo='echo -e \\t'

echo Remove old installation at $COMPACT_JVM_PATH
rm -r $COMPACT_JVM_PATH

#Remove env variable to JVM_PATH
sed -i '/COMPACT_JVM_PATH/d' ~/.profile    
echo export COMPACT_JVM_PATH= >> ~/.profile 

#Remove env variable to JVM_HOME
sed -i '/COMPACT_JVM_HOME/d' ~/.profile    
echo export COMPACT_JVM_HOME= >> ~/.profile

exit 0
