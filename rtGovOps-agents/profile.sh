#!/bin/sh

LOG_FILE=profile.log
DLM="="
alias echo='echo -e'

MAC=$(/sbin/ifconfig eth0 | grep -o -E '([[:xdigit:]]{1,2}:){5}[[:xdigit:]]{1,2}')
echo DEVICE-ID $DLM $MAC
echo 
echo 

echo Date/time at the device:
echo ----------------------------------
#echo date-time $DLM `date -R`,
echo date $DLM `date -I'date'`,
echo time $DLM `date -I'seconds' | awk '$0=$2' FS='T' RS='+'`,
echo up-time $DLM `uptime| awk '{ print $3 $4 $5}'`
echo load-avg-15min $DLM `uptime|awk '{print $10 }'`
echo 

echo General OS info:
echo ----------------------------------
echo os-name$DLM`uname -o`,
echo kernel-version$DLM`uname -r`,
#echo os-release $DLM `uname -r`
echo machine-name$DLM`uname -m`,
echo bussy-box-version$DLM`busybox | awk '{print $2}' | head -1`,
echo 

echo CPU info:
echo ----------------------------------
echo cpu-number $DLM `grep Processor /proc/cpuinfo | wc -l |awk '{print $1}'`,
echo cpu-name $DLM `grep Processor /proc/cpuinfo | awk '{ print $3}' | tail -1`,
echo cpu-speed $DLM `grep MIPS /proc/cpuinfo | awk '{ print $3}' | tail -1` BogoMIPS,
echo cpu-arch $DLM `grep architecture /proc/cpuinfo | awk '{ print $3}' | tail -1`,
echo 

echo Network info:
echo ----------------------------------
echo hostname $DLM `hostname`
for i in `ifconfig | grep "Link " | awk '{ print $1 }' | grep -v lo`
do
  echo "ip-address-$i $DLM `ifconfig $i | awk 'NR==2 { print $2}'|awk -F":" '{print $2}'`,"
done

echo open-ports $DLM `netstat -l -t -n | awk '{print $4}' | awk -F':' '{ print $2 }' ORS=' '`,
echo resolving-server $DLM `awk '{print $2}' /etc/resolv.conf`,
echo 

echo Storage info:
echo ----------------------------------
df -h | grep " /" | grep -v none | while read i; do 
  echo filesystem-total-`echo $i | awk '{ print$1}'` \( `echo  $i | awk '{ print$6}'` \) $DLM `echo $i | awk '{ print$2}'`,
  echo filesystem-avail-`echo $i | awk '{ print$1}'` \( `echo  $i | awk '{ print$6}'` \) $DLM `echo $i | awk '{ print$4}'`,
done
echo 

echo RAM info:
echo ----------------------------------
echo RAM-total $DLM `cat /proc/meminfo | grep MemTotal | awk '{ print$2}'` KB,
echo RAM-free $DLM `cat /proc/meminfo | grep MemFree | awk '{ print$2}'` KB,
echo 

echo User and process info:
echo ----------------------------------
echo list-of-users $DLM ` awk -F':' '{ print $1 }' ORS=' ' /etc/passwd`,
echo num-of-running-procs $DLM `ps | awk 'NR>1' | wc -l`,
#echo list-of-processes $DLM `ps -l | awk '$4 != "1" {print $10}' ORC=' '`,
echo list-of-daemons $DLM `top -n -1 | awk '$2 == "1" {print $8 $9 $10 $11}' ORC=' '`,
echo 

echo Environment info: 
echo ----------------------------------
env | tr ':' ' '





