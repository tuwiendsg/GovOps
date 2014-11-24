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
package at.ac.tuwien.infosys;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class BalancerTest {

	private final String balancerURL = "/balancer/assign/";

	private final String node1 = "128.130.172.231";
	private final String node2 = "128.130.172.174";

	private final String device1 = "ID1";
	private final String device2 = "ID2";
	private final String device3 = "ID3";
	private final String device4 = "ID4";
	private final String device5 = "ID5";

	private MockMvc mockMvc;
	
	@Value("${node.startup.time}")
	protected long nodeStartupTime;

	@Autowired
	WebApplicationContext wac;

	@Before
	public void setUp() throws Exception {
		// Process mock annotations
		MockitoAnnotations.initMocks(this);

		// Setup Spring test in webapp-mode (same config as spring-boot)
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_schedule() throws Exception {
		MvcResult mvcResult = invokeBalancer(device1);

		mvcResult.getAsyncResult();

		checkResult(mvcResult, node1);

		mvcResult = invokeBalancer(device2);

		mvcResult.getAsyncResult();

		checkResult(mvcResult, node1);
	}

	@Test
	public void test_schedule_start_idle() throws Exception {

		long start = System.currentTimeMillis();

		MvcResult mvcResult = invokeBalancer(device3);
		MvcResult mvcResult2 = invokeBalancer(device4);

		mvcResult.getAsyncResult();
		mvcResult2.getAsyncResult();

		long end = System.currentTimeMillis();

		assertThat(end - start, greaterThanOrEqualTo(nodeStartupTime));

		checkResult(mvcResult, node2);
		checkResult(mvcResult2, node2);
	}
	
	@Test
	public void test_schedule_no_node_available() throws Exception {

		MvcResult mvcResult = invokeBalancer(device5);

		mvcResult.getAsyncResult();

		checkResult(mvcResult, "No nodes available!");
	}

	private void checkResult(MvcResult mvcResult, String expectedContent)
			throws Exception {
		mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
				.andExpect(content().string(expectedContent));
	}

	private MvcResult invokeBalancer(String device) throws Exception {
		MvcResult mvcResult;
		mvcResult = this.mockMvc.perform(get(balancerURL + device))
				.andExpect(request().asyncStarted()).andReturn();
		return mvcResult;
	}

}
