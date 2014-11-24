#!/bin/sh

alias echo='echo -e \\t'

JAVA_SAMPLE_APP_HOME="/data/G2021/sample-java-app"
mkdir -p $JAVA_SAMPLE_APP_HOME
cp sample-app/artifacts/SampleClient.class $JAVA_SAMPLE_APP_HOME/SampleClient.class

chmod +x $JAVA_SAMPLE_APP_HOME

if [ $? -ne 0 ]
then
  echo Error installing application!
  exit 1
fi

sed -i '/JAVA_SAMPLE_APP_HOME/d' ~/.profile
echo export JAVA_SAMPLE_APP_HOME=$JAVA_SAMPLE_APP_HOME >> ~/.profile

exit 0
