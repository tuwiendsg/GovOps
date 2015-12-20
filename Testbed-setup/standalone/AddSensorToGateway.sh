#!/bin/bash

#$1-sensor id
#$2-gateway id
#assume pwd = same as SensorGatewayUtil

BASE_DIR="iCOMOT-simulated-devices"
GATEWAYS="$BASE_DIR/gateways"
SENSORS="$BASE_DIR/sensors"


if [ -z $1 ] && [ -n $1 ]
then
  log "No sensor id provided!"
  exit 0
else
  SENSOR_ID=$1
fi

if [ -z $2 ] && [ -n $2 ]
then
  log "No gateway id provided!"
  exit 0
else
  GATEWAY_ID=$2
fi

while test $# -gt 0; do
	case "$3" in		
		-m|--mqtt-broker)
			SET_MQTT_BROKER="true"
			BROKER_URL=$4

			shift 2
			;;
		"")
			break
			;;	  		
		*)
			echo "Wrong parameter $3"
			exit 1
    			;;
	esac
	
done

mkdir -p ./$GATEWAYS/$GATEWAY_ID/s-distribution
cp ./$SENSORS/$SENSOR_ID/$SENSOR_ID-distribution/runSensor_data_$SENSOR_ID.sh ./$GATEWAYS/$GATEWAY_ID/s-distribution/runSensor.sh
cp ./$SENSORS/$SENSOR_ID/$SENSOR_ID-distribution/sensor.tar.gz ./$GATEWAYS/$GATEWAY_ID/s-distribution/sensor.tar.gz

# Now add custom Dockerfile and entrypoint
#cp ./util/Dockerfile-local ./$GATEWAYS/$GATEWAY_ID/Dockerfile
cp ./util/starter_ubuntu-local.sh ./$GATEWAYS/$GATEWAY_ID/starter_ubuntu.sh

sed -i '/# Start cron deamon.*/i \
# Add the sensor stuff \
RUN echo "mqtt_broker=192.168.1.6" >> \/etc\/environment \
RUN mkdir \/usr\/share\/sensor \
ADD s-distribution\/* \/usr\/share\/sensor\/ \
\
' ./$GATEWAYS/$GATEWAY_ID/Dockerfile

#Comment out decomission - this is only for SALSA
sed "s/ADD .\/decommission \/bin\/decommission/#ADD .\/decommission \/bin\/decommission/g" -i ./$GATEWAYS/$GATEWAY_ID/Dockerfile

#Set base image to java
sed "s/FROM .*/FROM java:latest/g" -i ./$GATEWAYS/$GATEWAY_ID/Dockerfile


#Handle arguments
if [ "$SET_MQTT_BROKER" == "true" ]; then

sed "s/RUN echo \"mqtt_broker=.*/RUN echo \"mqtt_broker=$BROKER_URL\" >> \/etc\/environment/g" -i ./$GATEWAYS/$GATEWAY_ID/Dockerfile

#Run gatway (set port and gw type)
#d run -e SALSA_ENV_PORTMAP_80="`~/myIP.sh`:9000" -d  -p 192.168.1.6:9000:80 -t gateway/gwt1
fi

exit 0


