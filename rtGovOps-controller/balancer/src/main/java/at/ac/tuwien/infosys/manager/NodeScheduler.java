/*
 * Copyright (c) 2014 Technische Universitaet Wien (TUW), Distributed SystemsGroup E184.
 * 
 * This work was partially supported by the Pacific Controls under the Pacific Controls 
 * Cloud Computing Lab (pc3l.infosys.tuwien.ac.at)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Written by Michael Voegler
 */
package at.ac.tuwien.infosys.manager;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import at.ac.tuwien.infosys.model.Node;
import at.ac.tuwien.infosys.model.SchedulingRequest;
import at.ac.tuwien.infosys.strategy.BalancingStrategy;
import at.ac.tuwien.infosys.strategy.NoNodeAvailableException;

public class NodeScheduler implements Runnable {

	private BlockingQueue<SchedulingRequest> requestQueue;
	private Map<String, Node> runningNodes;
	private Map<String, Node> idleNodes;
	private BalancingStrategy balancingStrategy;
	private long nodeStartupTime;

	public NodeScheduler(BlockingQueue<SchedulingRequest> reqestQueue,
			Map<String, Node> runningNodes, Map<String, Node> idleNodes,
			BalancingStrategy balancingStrategy, long nodeStartupTime) {
		this.requestQueue = reqestQueue;
		this.runningNodes = runningNodes;
		this.idleNodes = idleNodes;
		this.balancingStrategy = balancingStrategy;
		this.nodeStartupTime = nodeStartupTime;
	}

	public void run() {
		try {
			while (true) {
				SchedulingRequest result = requestQueue.take();

				String node = null;
				try {
					// try to assign to running
					node = balancingStrategy.getNode(runningNodes.values());
				} catch (NoNodeAvailableException e) {
					// start an idle node
					if (!idleNodes.isEmpty()) {

						Node chosen = new ArrayList<Node>(idleNodes.values())
								.get(0);

						// start chosen node
						try {
							Thread.sleep(nodeStartupTime);
						} catch (InterruptedException i) {
						}

						chosen.setStarted(true);
						String nodeIP = chosen.getIp();

						idleNodes.remove(nodeIP);
						runningNodes.put(nodeIP, chosen);

						node = nodeIP;
					}
				}
				// sets result to null in case there is no node available
				// anymore!
				if (node != null) {
					runningNodes.get(node).addDevice(result.getDeviceId());
					result.getResult().setResult(node);
				} else
					result.getResult().setErrorResult("No nodes available!");
			}
		} catch (InterruptedException e) {
		}
	}

}
