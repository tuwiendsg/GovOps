#!/bin/sh

BASE="/usr/share/cgi-bin/capabilities"

invoke(){
  #Log invocation
  echo $1 >> $BASE/log 2>&1
  echo "{\"result\": {\"id\":\"`uname -n`\", \"capaName\":\"cTest\", \"capaResult\":\"$1\"}}"

}

if [ -z $1 ]
then
  invoke "No method to invoke... Will exit..."
  exit 0
elif [ -n $1 ]
then
  method=$1
fi

case $method in
   "method1") invoke "Method 1 invoked!";;
   "method2") invoke "Method 2 invoked!$2 $3 $4 ";;
   *) invoke "Unsupported method! $method";;
esac

exit 0