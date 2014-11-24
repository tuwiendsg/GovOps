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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.infosys.model.Node;
import at.ac.tuwien.infosys.model.ProvisionRequest;
import at.ac.tuwien.infosys.strategy.LoadBasedStrategy;

public class NodeManagerTest {

	private NodeManager nodeManager;

	private final String node1IP = "128.130.172.231";
	private final String node2IP = "128.130.172.232";
	private final String node3IP = "128.130.172.233";
	private final String device1ID = "device1ID";
	private final String device2ID = "device2ID";
	private final String device3ID = "device3ID";
	private final String device4ID = "device4ID";

	private final long nodeStartupTime = 1000;
	private final int loadThreshold = 2;

	@Before
	public void setUp() throws Exception {
		nodeManager = new NodeManager(new ArrayList<String>());
		nodeManager.nodeStartupTime = nodeStartupTime;
		nodeManager.balancingStrategy = new LoadBasedStrategy(loadThreshold);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInit() {

		List<String> nodeList = new ArrayList<String>();
		nodeList.add(node1IP);
		nodeList.add(node2IP);
		nodeList.add(node3IP);

		nodeManager = new NodeManager(nodeList);

		Map<String, Node> nodes = nodeManager.getRunningNodes();
		assertNotNull(nodes);
		assertThat(nodes.size(), equalTo(1));
		Node node = nodes.get(node1IP);
		assertNotNull(node);

		Node expected = new Node(node1IP, new ArrayList<String>());
		expected.setStarted(true);

		assertThat(node, equalTo(expected));

		// check for idle nodes

		nodes = nodeManager.getIdleNodes();
		assertNotNull(nodes);
		assertThat(nodes.size(), equalTo(2));
		node = nodes.get(node2IP);
		assertNotNull(node);

		expected = new Node(node2IP, new ArrayList<String>());
		assertThat(node, equalTo(expected));

		node = nodes.get(node3IP);
		assertNotNull(node);

		expected = new Node(node3IP, new ArrayList<String>());
		assertThat(node, equalTo(node));

	}

	@Test
	public void testResolve_with_known() {
		Node temp = new Node(node1IP, Arrays.asList(device1ID));
		nodeManager.getRunningNodes().put(node1IP, temp);

		String resolved = nodeManager.resolve(device1ID);

		assertThat(resolved, equalTo(node1IP));
	}

	@Test
	public void testResolve_with_known_extended() {
		Node temp = new Node(node1IP, Arrays.asList(device1ID, device2ID));
		Node temp2 = new Node(node2IP, Arrays.asList(device3ID));
		nodeManager.getRunningNodes().put(node1IP, temp);
		nodeManager.getRunningNodes().put(node2IP, temp2);

		String resolved = nodeManager.resolve(device1ID);
		String resolved2 = nodeManager.resolve(device2ID);
		String resolved3 = nodeManager.resolve(device3ID);

		assertNotNull(resolved);
		assertThat(resolved, equalTo(node1IP));

		assertNotNull(resolved2);
		assertThat(resolved2, equalTo(node1IP));

		assertNotNull(resolved3);
		assertThat(resolved3, equalTo(node2IP));
	}

	@Test
	public void testResolve_with_unknown() {
		Node temp = new Node(node1IP, Arrays.asList(device1ID));
		nodeManager.getRunningNodes().put(node1IP, temp);

		String resolved = nodeManager.resolve("device2ID");

		assertNull(resolved);
	}

	@Test
	public void test_provision_noRunningNodes() {
		assertThat(nodeManager.getRunningNodes().size(), equalTo(0));
		List<ProvisionRequest> ret = nodeManager.provision(10);
		assertNotNull(ret);
		assertThat(ret.size(), equalTo(0));
	}

	@Test
	public void test_provision_oneRunningNode_noDevices() {
		nodeManager.getRunningNodes().put(node1IP,
				new Node(node1IP, new ArrayList<String>()));
		assertThat(nodeManager.getRunningNodes().size(), equalTo(1));
		List<ProvisionRequest> ret = nodeManager.provision(10);
		assertNotNull(ret);
		assertThat(ret.size(), equalTo(0));
	}

	@Test
	public void test_provision_oneRunningNode_enoughDevices() {
		List<String> managedDevices = Arrays
				.asList(device1ID, device2ID, device3ID);
		
		nodeManager.getRunningNodes().put(
				node1IP,
				new Node(node1IP, managedDevices));
		assertThat(nodeManager.getRunningNodes().size(), equalTo(1));
		List<ProvisionRequest> ret = nodeManager.provision(2);
		assertNotNull(ret);
		assertThat(ret.size(), equalTo(1));
		assertThat(ret.get(0), equalTo(new ProvisionRequest(node1IP, Arrays.asList(device1ID, device2ID))));
	}
	
	@Test
	public void test_provision_oneRunningNode_thresholdDevices() {
		List<String> managedDevices = Arrays
				.asList(device1ID, device2ID);
		
		nodeManager.getRunningNodes().put(
				node1IP,
				new Node(node1IP, managedDevices));
		assertThat(nodeManager.getRunningNodes().size(), equalTo(1));
		List<ProvisionRequest> ret = nodeManager.provision(2);
		assertNotNull(ret);
		assertThat(ret.size(), equalTo(1));
		assertThat(ret.get(0), equalTo(new ProvisionRequest(node1IP, managedDevices)));
	}
	
	@Test
	public void test_provision_oneRunningNode_notEnoughDevices() {
		List<String> managedDevices = Arrays
				.asList(device1ID, device2ID, device3ID);
		
		nodeManager.getRunningNodes().put(
				node1IP,
				new Node(node1IP, managedDevices));
		assertThat(nodeManager.getRunningNodes().size(), equalTo(1));
		List<ProvisionRequest> ret = nodeManager.provision(4);
		assertNotNull(ret);
		assertThat(ret.size(), equalTo(1));
		assertThat(ret.get(0), equalTo(new ProvisionRequest(node1IP, managedDevices)));
	}
	
	@Test
	public void test_provision_twoRunningNodes() {
		List<String> managedDevicesNode1 = Arrays
				.asList(device1ID, device2ID);
		
		List<String> managedDevicesNode2 = Arrays
				.asList(device3ID);
		
		nodeManager.getRunningNodes().put(
				node1IP,
				new Node(node1IP, managedDevicesNode1));
		nodeManager.getRunningNodes().put(
				node2IP,
				new Node(node2IP, managedDevicesNode2));
		assertThat(nodeManager.getRunningNodes().size(), equalTo(2));
		List<ProvisionRequest> ret = nodeManager.provision(3);
		assertNotNull(ret);
		assertThat(ret.size(), equalTo(2));
		assertThat(ret.get(1), equalTo(new ProvisionRequest(node1IP, managedDevicesNode1)));
		assertThat(ret.get(0), equalTo(new ProvisionRequest(node2IP, managedDevicesNode2)));
	}
	
	@Test
	public void test_provision_twoRunningNodesExtended() {
		List<String> managedDevicesNode1 = Arrays
				.asList(device1ID, device2ID);
		
		List<String> managedDevicesNode2 = Arrays
				.asList(device3ID, device4ID);
		
		nodeManager.getRunningNodes().put(
				node1IP,
				new Node(node1IP, managedDevicesNode1));
		nodeManager.getRunningNodes().put(
				node2IP,
				new Node(node2IP, managedDevicesNode2));
		assertThat(nodeManager.getRunningNodes().size(), equalTo(2));
		List<ProvisionRequest> ret = nodeManager.provision(3);
		assertNotNull(ret);
		assertThat(ret.size(), equalTo(2));
		assertThat(ret.get(1), equalTo(new ProvisionRequest(node1IP, Arrays.asList(device1ID))));
		assertThat(ret.get(0), equalTo(new ProvisionRequest(node2IP, managedDevicesNode2)));
	}

	// @Test
	// public void testScheduleNode() throws Exception {
	// Node temp = new Node(node1IP, new ArrayList<String>());
	// temp.setStarted(true);
	// nodeManager.getRunningNodes().put(node1IP, temp);
	//
	// assertThat(nodeManager.getIdleNodes().size(), equalTo(2));
	// assertThat(nodeManager.getRunningNodes().size(), equalTo(1));
	//
	// DeferredResult<String> ret = nodeManager.scheduleNode(device1ID);
	//
	// Thread.sleep(2000);
	//
	// String scheduledNodeIP = (String) ret.getResult();
	// assertNotNull(scheduledNodeIP);
	// assertThat(scheduledNodeIP, equalTo(node1IP));
	// assertThat(temp.getDevices().size(), equalTo(1));
	// assertTrue(temp.getDevices().contains(device1ID));
	// }
	//
	// @Test
	// public void testScheduleNode_extended() {
	// Node temp = new Node(node1IP, new ArrayList<String>());
	// temp.setStarted(true);
	// Node temp2 = new Node(node2IP, new ArrayList<String>());
	// nodeManager.getRunningNodes().put(node1IP, temp);
	// nodeManager.getIdleNodes().put(node2IP, temp2);
	//
	// assertThat(nodeManager.getIdleNodes().size(), equalTo(1));
	// assertThat(nodeManager.getRunningNodes().size(), equalTo(1));
	//
	// String scheduledNodeIP = (String) nodeManager.scheduleNode(device1ID)
	// .getResult();
	// assertNotNull(scheduledNodeIP);
	// assertThat(scheduledNodeIP, equalTo(node1IP));
	// assertThat(temp.getDevices().size(), equalTo(1));
	// assertTrue(temp.getDevices().contains(device1ID));
	//
	// scheduledNodeIP = (String) nodeManager.scheduleNode(device2ID)
	// .getResult();
	// assertNotNull(scheduledNodeIP);
	// assertThat(scheduledNodeIP, equalTo(node1IP));
	// assertThat(temp.getDevices().size(), equalTo(2));
	// assertTrue(temp.getDevices().contains(device2ID));
	//
	// scheduledNodeIP = (String) nodeManager.scheduleNode(device3ID)
	// .getResult();
	// assertNotNull(scheduledNodeIP);
	// assertThat(scheduledNodeIP, equalTo(node2IP));
	// assertThat(temp2.getDevices().size(), equalTo(1));
	// assertTrue(temp2.getDevices().contains(device3ID));
	// assertThat(temp.getDevices().size(), equalTo(2));
	// assertTrue(temp.getDevices().contains(device2ID));
	// }

}
