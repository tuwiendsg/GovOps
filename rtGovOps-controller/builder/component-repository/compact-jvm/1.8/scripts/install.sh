#!/bin/sh

alias echo='echo -e \\t'

COMPACT_JVM_PATH="/data/G2021/compact-jvm"
COMPACT_JVM_HOME="$COMPACT_JVM_PATH/bin"

mkdir -p $COMPACT_JVM_PATH
unzip compact-jvm/artifacts/compact-jvm.zip -d $COMPACT_JVM_PATH/

chmod +x $COMPACT_JVM_PATH
chmod +x -R $COMPACT_JVM_HOME

if [ $? -ne 0 ]
then
  echo Error installing compact-jvm!
  exit 1
fi

sed -i '/COMPACT_JVM_PATH/d' ~/.profile
echo export COMPACT_JVM_PATH=$COMPACT_JVM_PATH >> ~/.profile

sed -i '/COMPACT_JVM_HOME/d' ~/.profile
echo export COMPACT_JVM_HOME=$COMPACT_JVM_HOME >> ~/.profile

exit 0
