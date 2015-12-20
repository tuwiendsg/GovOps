#!/bin/sh

echo $SALSA_ENV_PORTMAP_80
echo "SALSA_ENV_PORTMAP_80=$SALSA_ENV_PORTMAP_80" >> /etc/environment

#Run the agent only once to register with rtGovOps server
touch /usr/share/provi-agent/agent.log
/bin/bash -c "/usr/share/provi-agent/agent.sh > /usr/share/provi-agent/agent.log"



# Start Apache on Ubuntu
/usr/sbin/a2enmod cgi
/usr/sbin/apache2ctl start

chown www-data /bin/kill

# Install sensor 
chmod +x /usr/share/sensor/runSensor.sh
/bin/bash -c 'cd /usr/share/sensor; /usr/share/sensor/runSensor.sh'

# Run sensor (Stop sensor will not work through GovOps API)
chmod +x /tmp/sensor/container_run.sh
/bin/bash -c 'cd /tmp/sensor; ./container_run_bg.sh'

# Hold on to process
tail -f /usr/share/provi-agent/agent.log


exit 0
