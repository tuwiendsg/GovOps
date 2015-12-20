#!/bin/sh

#i="0"
#while [ $i -lt 120 ]
#do
#	echo Running in container X-$i at time `date` >> /root/cron_log 2>&1
#	let i=i+1
#	sleep 1
#done

/usr/bin/gcc /usr/lib/cgi-bin/capabilities/elevate.c -o /usr/lib/cgi-bin/capabilities/elevate

# Start httpd on busybox
#/usr/sbin/httpd -k start

# Start Apache on Ubuntu
/usr/sbin/a2enmod cgi
/usr/sbin/apache2ctl start

chown www-data /bin/kill

# Start location service
#/usr/bin/java -cp "/usr/share/location-service/*" container.Main


#/bin/sh -c crond start && tail -F /usr/share/provi-agent/cron_log
tail -F /usr/share/provi-agent/cron_log

exit 0