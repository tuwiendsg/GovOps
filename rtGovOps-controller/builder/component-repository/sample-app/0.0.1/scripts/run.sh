#!/bin/sh

alias echo='echo -e \\t'

if env | grep -q ^COMPACT_JVM_HOME= || [[ ! -z $COMPACT_JVM_HOME ]]
then
  echo COMPACT_JVM_HOME home is $COMPACT_JVM_HOME
else
  echo COMPACT_JVM_HOME must be set to run Java Sample App!
  exit 1
fi

(
cd $JAVA_SAMPLE_APP_HOME
exec $COMPACT_JVM_HOME/java SampleClient http://128.130.172.231:8085/SDGSampleServer/client/ sampleClient 2000 50 > ~/javaAPP.out 2>&1 &
)

exit 0
