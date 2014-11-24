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
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import at.ac.tuwien.infosys.model.Node;
import at.ac.tuwien.infosys.model.ProvisionRequest;
import at.ac.tuwien.infosys.model.SchedulingRequest;
import at.ac.tuwien.infosys.strategy.BalancingStrategy;
import at.ac.tuwien.infosys.strategy.LoadBasedStrategy;

@Component
@Scope(value = "singleton")
public class NodeManager implements INodeManager {

	private Map<String, Node> runningNodes = new ConcurrentHashMap<String, Node>();
	private Map<String, Node> idleNodes = new ConcurrentHashMap<String, Node>();

	private BlockingQueue<SchedulingRequest> schedulingRequests = new LinkedBlockingQueue<SchedulingRequest>();

	private NodeScheduler nodeScheduler;

	protected BalancingStrategy balancingStrategy;

	@Value("${node.startup.time}")
	protected long nodeStartupTime;
	@Value("${load.threshold}")
	protected int loadThreshold;
	@Value("#{'${nodes}'.split(',')}")
	protected List<String> nodeList;

	private Thread scheduler;

	public NodeManager() {
	}

	public NodeManager(List<String> nodeList) {
		this.nodeList = nodeList;
		init();
	}

	@PostConstruct
	private void init() {
		// first node of list will be started, other remain idle (i.e. offline)
		for (String node : nodeList) {
			Node n = new Node(node, new ArrayList<String>());
			if (runningNodes.isEmpty()) {
				n.setStarted(true);
				runningNodes.put(node, n);
			} else
				idleNodes.put(node, n);
		}

		balancingStrategy = new LoadBasedStrategy(loadThreshold);
		nodeScheduler = new NodeScheduler(schedulingRequests, runningNodes,
				idleNodes, balancingStrategy, nodeStartupTime);

		if (scheduler == null)
			scheduler = new Thread(nodeScheduler);

		if (!scheduler.isAlive())
			scheduler.start();
	}

	@PreDestroy
	public void clean() {
		scheduler.interrupt();
	}

	/**
	 * Resolves the node that manages a device with the given id.
	 * 
	 * @param device
	 * @return
	 */
	public synchronized String resolve(String device) {
		for (Node n : runningNodes.values())
			if (n.isManagedDevice(device))
				return n.getIp();

		return null;
	}

	public List<String> getAllNodes() {
		List<String> ret = new ArrayList<String>(runningNodes.keySet());
		ret.addAll(new ArrayList<String>(idleNodes.keySet()));
		return ret;
	}

	public List<String> getAvailableNodes() {
		List<String> ret = new ArrayList<String>(runningNodes.keySet());
		return ret;
	}

	public DeferredResult<String> scheduleNode(String deviceId) {
		DeferredResult<String> result = new DeferredResult<String>();
		SchedulingRequest request = new SchedulingRequest(deviceId, result);

		// check if device was already scheduled
		String node = resolve(deviceId);
		if (node != null)
			result.setResult(node);
		else
			try {
				schedulingRequests.put(request);
			} catch (InterruptedException e) {
			}

		return result;
	}

	public List<ProvisionRequest> provision(int nrOfDevices) {

		List<ProvisionRequest> ret = new ArrayList<ProvisionRequest>();
		List<Node> running = new ArrayList<Node>(runningNodes.values());

		for (Node node : running) {
			List<String> selectedIds = null;

			if (nrOfDevices == 0 || node.getDevices().isEmpty())
				break;

			if (nrOfDevices <= node.getNumberofManagedDevices())
				selectedIds = new ArrayList<String>(node.getDevices().subList(
						0, nrOfDevices));

			if (nrOfDevices > node.getNumberofManagedDevices())
				selectedIds = new ArrayList<String>(node.getDevices());

			ProvisionRequest request = new ProvisionRequest(node.getIp(),
					selectedIds);
			ret.add(request);
			nrOfDevices = nrOfDevices - selectedIds.size();
		}

		return ret;
	}

	public void reset() {
		runningNodes.clear();
		idleNodes.clear();
		schedulingRequests.clear();
		init();
	}

	public Map<String, Node> getRunningNodes() {
		return runningNodes;
	}

	public void setRunningNodes(Map<String, Node> nodes) {
		this.runningNodes = nodes;
	}

	public Map<String, Node> getIdleNodes() {
		return idleNodes;
	}

	public void setIdleNodes(Map<String, Node> idleNodes) {
		this.idleNodes = idleNodes;
	}

}
