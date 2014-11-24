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
package at.ac.tuwien.infosys.logger;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import at.ac.tuwien.infosys.Application;
import at.ac.tuwien.infosys.model.Log;
import at.ac.tuwien.infosys.model.Statistic;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ProvisioningLoggerTest {

	private final String id1 = "ID1";
	private final String id2 = "ID2";
	private final String id3 = "ID3";

	private final long temp = System.currentTimeMillis();
	private final long started = temp - 1000;
	private final long finished = temp;

	@Autowired
	private ProvisioningLogger provisioningLogger;

	@Before
	public void setUp() {
		provisioningLogger.getDeviceLogs().clear();
		provisioningLogger.setStartingTime(0);
	}

	@Test
	public void test_startLogging() {
		List<String> deviceIds = Arrays.asList(id1, id2, id3);
		long start = System.currentTimeMillis();

		assertThat(provisioningLogger.getDeviceLogs().size(), equalTo(0));

		provisioningLogger.startLogging(deviceIds, start);

		assertThat(provisioningLogger.getDeviceLogs().size(), equalTo(3));
		assertNull(provisioningLogger.getDeviceLogs().get(id1));
		assertNull(provisioningLogger.getDeviceLogs().get(id2));
		assertNull(provisioningLogger.getDeviceLogs().get(id3));
	}

	@Test
	public void test_addLog() {

		assertThat(provisioningLogger.getDeviceLogs().size(), equalTo(0));
		provisioningLogger.getDeviceLogs().put(id1, null);
		provisioningLogger.setStartingTime(started);

		provisioningLogger.addLog(id1, finished);

		assertThat(provisioningLogger.getDeviceLogs().size(), equalTo(1));
		Log log = provisioningLogger.getDeviceLogs().get(id1);

		assertNotNull(log);
		assertThat(log, equalTo(new Log(id1, started, finished)));

	}

	@Test
	public void test_allFinished_stillRunning() {

		assertThat(provisioningLogger.getDeviceLogs().size(), equalTo(0));
		provisioningLogger.getDeviceLogs().put(id1, null);
		provisioningLogger.setStartingTime(started);

		boolean ret = provisioningLogger.allFinished();
		assertFalse(ret);
	}

	@Test
	public void test_allFinished() {

		assertThat(provisioningLogger.getDeviceLogs().size(), equalTo(0));
		provisioningLogger.getDeviceLogs().put(id1,
				new Log(id1, started, finished));
		provisioningLogger.setStartingTime(started);

		boolean ret = provisioningLogger.allFinished();
		assertTrue(ret);
	}

	@Test
	public void test_allFinished_oneRunning() {

		assertThat(provisioningLogger.getDeviceLogs().size(), equalTo(0));
		provisioningLogger.getDeviceLogs().put(id1,
				new Log(id1, started, finished));
		provisioningLogger.getDeviceLogs().put(id2, null);
		provisioningLogger.setStartingTime(started);

		boolean ret = provisioningLogger.allFinished();
		assertFalse(ret);
	}

	@Test
	public void test_getLogs_empty() {
		List<Log> logs = provisioningLogger.getLogs();
		assertNotNull(logs);
		assertThat(logs.size(), equalTo(0));
	}

	@Test
	public void test_getLogs_oneRunning() {
		provisioningLogger.getDeviceLogs().put(id2, null);
		provisioningLogger.setStartingTime(started);

		List<Log> logs = provisioningLogger.getLogs();
		assertNotNull(logs);
		assertThat(logs.size(), equalTo(0));
	}

	@Test
	public void test_getLogs_oneFinished() {
		provisioningLogger.getDeviceLogs().put(id1,
				new Log(id1, started, finished));
		provisioningLogger.getDeviceLogs().put(id2, null);
		provisioningLogger.setStartingTime(started);

		List<Log> logs = provisioningLogger.getLogs();
		assertNotNull(logs);
		assertThat(logs.size(), equalTo(1));
		Log log = logs.get(0);
		assertNotNull(log);
		assertThat(log, equalTo(new Log(id1, started, finished)));
	}

	@Test
	public void test_getStatistics_empty() {
		provisioningLogger.setStartingTime(started);
		Statistic statistic = provisioningLogger.getStatistic();
		assertNotNull(statistic);
		assertThat(statistic.getStarted(), equalTo(started));
		assertThat(statistic.getLogs().size(), equalTo(0));
		assertThat(statistic.getFinished(), equalTo(0l));
	}

	@Test
	public void test_getStatistics_oneRunning() {
		provisioningLogger.getDeviceLogs().put(id2, null);
		provisioningLogger.setStartingTime(started);
		Statistic statistic = provisioningLogger.getStatistic();
		assertNotNull(statistic);
		assertThat(statistic.getStarted(), equalTo(started));
		assertThat(statistic.getLogs().size(), equalTo(0));
		assertThat(statistic.getFinished(), equalTo(0l));
	}

	@Test
	public void test_getStatistics_oneFinished() {
		provisioningLogger.getDeviceLogs().put(id1,
				new Log(id1, started, finished));
		provisioningLogger.setStartingTime(started);
		Statistic statistic = provisioningLogger.getStatistic();
		assertNotNull(statistic);
		assertThat(statistic.getStarted(), equalTo(started));
		assertThat(statistic.getLogs().size(), equalTo(1));
		assertThat(statistic.getFinished(), equalTo(finished));
	}

	@Test
	public void test_getStatistics_twoFinished() {
		provisioningLogger.getDeviceLogs().put(id1,
				new Log(id1, started, finished));
		provisioningLogger.getDeviceLogs().put(id2,
				new Log(id1, started, finished + 2000));
		provisioningLogger.setStartingTime(started);
		Statistic statistic = provisioningLogger.getStatistic();
		assertNotNull(statistic);
		assertThat(statistic.getStarted(), equalTo(started));
		assertThat(statistic.getLogs().size(), equalTo(2));
		assertThat(statistic.getFinished(), equalTo(finished + 2000));
	}
}
