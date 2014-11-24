#!/bin/sh
REPO_NAME=component-repository
DEST_DIR=/tmp/

function install_repo() {
	nodes=("$@")
        path=${nodes[0]}
        unset nodes[0]
	(
		cd $path

		for node in "${nodes[@]}"
		do
			echo "##########################################"
			echo "Install component-repo on: $node"
			echo "##########################################"
			scp -r $REPO_NAME ubuntu@$node:$DEST_DIR
			echo "##########################################"
		done
	)
}
