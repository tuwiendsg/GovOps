#/bin/sh

JPID=$(ps aux |grep java| grep -v grep | awk '{print $2}')
kill $JPID

exit 0