#!/bin/bash
function deploy() {
	nodes=("$@")
        path=${nodes[0]}
        unset nodes[0]
	echo "Nodes: $nodes"
	(
		cd $path
		for node in "${nodes[@]}"
		do
			echo "##########################################"
			echo "Deploy project: $path to node: $node"
			echo "##########################################"
			mvn tomcat7:deploy -DskipTests -Dtomcat.ip=$node
			echo "##########################################"
		done
	)
}

function undeploy() {
	nodes=("$@")
	path=${nodes[0]}
	unset nodes[0]
	echo "Nodes: ${nodes[@]}"
	(
		cd $path
		for node in "${nodes[@]}"
		do
			echo "##########################################"
			echo "Undeploy project: $path from node: $node"
			echo "##########################################"
			mvn tomcat7:undeploy -DskipTests -Dtomcat.ip=$node
			echo "##########################################"
		done
	)
}

function redeploy() {
	nodes=("$@")
        path=${nodes[0]}
        unset nodes[0]
	(
		cd $path
		for node in "${nodes[@]}"
		do
			echo "##########################################"
			echo "Redeploy project: $path to node: $node"
			echo "##########################################"
			mvn tomcat7:redeploy -DskipTests -Dtomcat.ip=$node
			echo "##########################################"
		done
	)
}
