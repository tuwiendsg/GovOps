#!/bin/bash

#This will create $1 govops box services (on 9000+1 ... 9000+$1 ports) in the cluster.
if [ -z $1 ]
then
  echo "Please specify how many services you want to run!"
  exit 0
elif [ -n $1 ]
then
  containerNo=$1
fi

instancesDir=/home/core/instances
rm -r $instancesDir > /dev/null 2>$1
mkdir $instancesDir > /dev/null 2>$1

for (( i=1; i<=$containerNo; i++ ))
do
port=$(($2+i))
	
	ln -s /home/core/configuration/gbox\@.service $instancesDir/gbox\@$port.service

	#fleetctl start /home/core/instances/gbox\@$port.service
	#fleetctl journal gbox@$port
done

echo -e "Successfully created:\n`ls $instancesDir`"
