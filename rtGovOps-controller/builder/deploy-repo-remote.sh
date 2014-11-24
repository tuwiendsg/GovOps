#!/bin/sh
REPO_NAME=component-repository
DEST_DIR=/tmp/

echo "Install component-repo on remote environment"
 
scp -r $REPO_NAME ubuntu@128.130.172.231:$DEST_DIR