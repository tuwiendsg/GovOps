#!/bin/bash
 
alias fleetctl="fleetctl  --strict-host-key-checking=false"
echo "First stop all known services ..."
fleetctl stop ./instances/*
echo
echo "Remove stopped services from registry ..."
fleetctl destroy ./instances/*
echo
echo "Start services..."
fleetctl start ./instances/*

echo =============================
echo List all services once more:
fleetctl list-units

echo "That's it ..."
exit 0
  
