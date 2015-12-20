#!/bin/sh

if [ -z "$1" ]
then
echo "Base must be provided!"
exit 0;
fi
#BASE=9000
BASE=$1
COAP=$(($BASE+100))
#currently mapped to HOST PUBLIC
COREOS_PRIVATE_IPV4=$(cat /etc/environment | grep COREOS_PRIVATE_IPV4| tr '=' ' '|awk '{print $2}')
COREOS_PUBLIC_IPV4=$(cat /etc/environment | grep COREOS_PUBLIC_IPV4| tr '=' ' '|awk '{print $2}'|tr '.' '_')
CONTAINER_ID=$(echo $COREOS_PRIVATE_IPV4| tr '.' '_')

for (( i=1; i<=200; i++ ))
do
#docker run -h "HOST_IP:9000" --name gbox.9000 -p HOST_IP:9000:80 -d govops_box
	docker run -h "$CONTAINER_ID:$(($BASE+$i))" --name gbox.$(($BASE+$i)) -p $COREOS_PRIVATE_IPV4:$(($BASE+$i)):80 -d govops_box
	echo "Created container $CONTAINER_ID:$(($BASE+$i))"
	sleep 1
done

echo "Done creating containers!"
exit 0