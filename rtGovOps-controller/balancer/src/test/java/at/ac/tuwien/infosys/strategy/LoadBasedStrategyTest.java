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
package at.ac.tuwien.infosys.strategy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.infosys.model.Node;

public class LoadBasedStrategyTest {

	private BalancingStrategy balancingStrategy;
	private final int loadThreshold = 2;

	private final String node1IP = "128.130.172.231";
	private final String node2IP = "128.130.172.232";

	private Node node1;
	private Node node2;
	private Collection<Node> nodes;

	@Before
	public void setUp() throws Exception {
		balancingStrategy = new LoadBasedStrategy(loadThreshold);

		node1 = new Node(node1IP, new ArrayList<String>());
		node2 = new Node(node2IP, new ArrayList<String>());

		nodes = new ArrayList<Node>();
		nodes.add(node1);
		nodes.add(node2);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBalancing_underload() throws NoNodeAvailableException {
		String result = balancingStrategy.getNode(nodes);

		assertNotNull(result);
		assertThat(result, equalTo(node1IP));

		node1.addDevice("device1Id");

		result = balancingStrategy.getNode(nodes);

		assertNotNull(result);
		assertThat(result, equalTo(node1IP));
	}

	@Test
	public void testBalancing_close_to_threshold()
			throws NoNodeAvailableException {
		node1.addDevice("device1Id");
		node1.addDevice("device2Id");

		String result = balancingStrategy.getNode(nodes);

		assertNotNull(result);
		assertThat(result, equalTo(node2IP));
	}

	@Test(expected = NoNodeAvailableException.class)
	public void testBalancing_overload() throws NoNodeAvailableException {
		node1.addDevice("device1Id");
		node1.addDevice("device2Id");
		node2.addDevice("device3Id");

		String result = balancingStrategy.getNode(nodes);

		assertNotNull(result);
		assertThat(result, equalTo(node2IP));

		node2.addDevice("device4Id");

		result = balancingStrategy.getNode(nodes);
	}
}
