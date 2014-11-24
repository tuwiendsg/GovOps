#!/bin/bash
BASE_DIR=$(cd $(dirname $0); pwd)
ROOT_DIR=$(cd $BASE_DIR/..; pwd)

BALANCER_DIR=$ROOT_DIR/balancer
MANAGER_DIR=$ROOT_DIR/manager
BUILDER_DIR=$ROOT_DIR/builder
API_DIR=$ROOT_DIR/apimanager
BUILDER_SCRIPTS=$BUILDER_DIR/scripts

chain_nodes=( "10.99.0.51" "10.99.0.16" )
balancer_nodes=( "localhost" )

echo $BASE_DIR
echo $ROOT_DIR
echo $BALANCER_DIR
echo $MANAGER_DIR
echo $BUILDER_DIR

source $BASE_DIR/node_management.sh
source $BUILDER_SCRIPTS/repository.sh

echo $BUILDER_DIR

# undeploy builder to chain of nodes
undeploy $BUILDER_DIR ${chain_nodes[@]}
deploy $BUILDER_DIR ${chain_nodes[@]}
# install component-repository for builder
install_repo $BUILDER_DIR ${chain_nodes[@]}

# deploy manager to chain of nodes
undeploy $MANAGER_DIR ${chain_nodes[@]}
deploy $MANAGER_DIR ${chain_nodes[@]}

# deploy manager to chain of nodes
undeploy $MANAGER_DIR ${chain_nodes[@]}
deploy $MANAGER_DIR ${chain_nodes[@]}

# deploy apimanager to chain of nodes
undeploy $API_DIR ${chain_nodes[@]}
deploy $API_DIR ${chain_nodes[@]}

# deploy balancer on localhost
undeploy $BALANCER_DIR $balancer_nodes
deploy $BALANCER_DIR $balancer_nodes
