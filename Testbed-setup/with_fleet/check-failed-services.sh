#!/bin/bash

#Some possible errors
# - If there is any authentication problem you have not sourced the init-cluster script
# - If Docker complains file not found you have not copied files to all machines in the cluster
# - If Docker log looks good, but services says Failed to start ... you need to increase the service startup timeout

echo
echo "Following services are not active:"
fleetctl list-units |grep -v active|grep -v ACTIVE | awk '{print $1 " "  $6}'

echo
echo "Open individual journals!"

logs=$(mktemp)
echo -e "$(fleetctl list-units |grep -v active|grep -vw "activating"|grep -v ACTIVE | awk '{print $1}') \n">> $logs

readarray a < $logs

for service in "${a[@]}" 
do
  echo "Open journal for $service";
  fleetctl journal $service 
  echo 
done
 
rm $logs

echo Done!
exit 0