#!/bin/sh



# Start httpd on busybox
/usr/sbin/httpd -k start

#Allow APIManager to trigger capability installation
chown nobody /usr/share/cgi-bin/capabilities

mkdir /tmp/agent
chown nobody /tmp/agent 

# Start location service
#/usr/bin/java -cp "/usr/share/location-service/*" container.Main


#/bin/sh -c crond start && tail -F /usr/share/provi-agent/cron_log

# run the agent
while true
do
	#echo Running in container X-$i at time `date` >> /root/cron_log 2>&1
	/usr/share/provi-agent/agent.sh >> /root/cron_log 2>&1
	sleep $(($RANDOM%10))
done


exit 0