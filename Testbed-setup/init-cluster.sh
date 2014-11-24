#!/bin/sh

export TERM=linux

source /etc/environment

#Change key permission so SSH does not complain
chmod 700 *.pem

#Recreate agent to avoid missing SSH_AUTH_SOCK bug
eval $(ssh-agent -s)
ssh-add *.pem

#Add all machines in the cluster to known hosts so fleet shh does not ask

#Give Dockerfile and context to all hosts in the cluster

fleetctl list-machines|awk '{print $2}'|grep -v IP|grep -v $COREOS_PRIVATE_IPV4|while read i; do
  echo "Upload Docker context to $i"
  echo yes | scp -r ./container_scripts/ core@$i:/home/core/
  echo
done

echo "Forwarding SSH agent to all machines in the cluster..."
# Inject forward agent to each machine with fleetctl ssh to avoid it failing later
fleetctl list-machines --no-legend --full |grep -v $COREOS_PRIVATE_IPV4|awk '{print $1}'|while read machine; do
  echo yes | fleetctl ssh -A $machine "touch .forwarded; exit"
  echo
done
