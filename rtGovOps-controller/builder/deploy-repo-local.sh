#!/bin/sh
REPO_NAME=component-repository
DEST_DIR=/tmp/$REPO_NAME
SRC_DIR=component-repository

echo "Install component-repo on local environment"

rm -rf $DEST_DIR
cp -r $SRC_DIR $DEST_DIR