FROM progrium/busybox
MAINTAINER Stefan Nastic

# Patch BB
COPY functions.sh /lib/functions.sh
RUN chmod a+x /lib/functions.sh
COPY opkg.conf /etc/opkg.conf
RUN chmod a+x /etc/opkg.conf

#Add httpd (apache 2.2.29)
#LOGS:
#	/tmp/error_log
#	/tmp/access_log
RUN opkg-install apache
ADD ./index.html /usr/share/htdocs/index.html
#scripts are in /usr/share/cgi-bin/
ADD ./mapper.sh /usr/share/cgi-bin/mapper
RUN chmod a+x /usr/share/cgi-bin/mapper

#Add some test capabilities
COPY capabilities/ /usr/share/cgi-bin/capabilities/
RUN chmod a+x -R /usr/share/cgi-bin/capabilities/
RUN touch /usr/share/cgi-bin/capabilities/log
RUN chmod a+rw /usr/share/cgi-bin/capabilities/log

# Add deployment agent 
RUN mkdir /usr/share/provi-agent/
RUN chmod a+x /usr/share/provi-agent/
ADD ./agent.sh /usr/share/provi-agent/agent.sh
ADD ./profile.sh /usr/share/provi-agent/profile.sh
ADD ./starter.sh /usr/share/provi-agent/starter.sh

# Create a cron task for the agent
ADD ./agent_task /usr/share/provi-agent/agent_task
RUN mkdir -p /var/spool/cron/crontabs
RUN touch root
RUN crontab /usr/share/provi-agent/agent_task
RUN touch /usr/share/provi-agent/cron_log
RUN rm /usr/share/provi-agent/agent_task

# Add FMS location service
#RUN mkdir /usr/share/location-service/
# COPY location-service/ /usr/share/location-service/
# RUN chmod a+x -R /usr/share/location-service/
# RUN touch /etc/environment
# RUN chmod 777 /etc/environment
# RUN echo "mqtt-broker=10.99.0.71">>/etc/environment 


# Monit
#ADD ./monitrc /etc/monitrc
#RUN chmod 600 /etc/monitrc
#RUN echo "NETWORKING=yes" > /etc/sysconfig/network

# Open ports 80 (REST server) 2812 (Monit) 5683 (CoAP Server)
EXPOSE 80 2812 5683

# Start cron deamon and hold on to a process
#CMD ["crond", "start"] 
#CMD crond start && tail -F /root/cron_log

RUN chmod +x /usr/share/provi-agent/starter.sh
CMD ["/bin/sh", "/usr/share/provi-agent/starter.sh"] 
