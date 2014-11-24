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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import at.ac.tuwien.infosys.model.Log;
import at.ac.tuwien.infosys.model.Statistic;

@Component(value = "singleton")
public class ProvisioningLogger implements ILogger {

	private long startingTime;
	private Map<String, Log> deviceLogs = Collections
			.synchronizedMap(new HashMap<String, Log>());

	public ProvisioningLogger() {
	}

	@Override
	public void startLogging(List<String> deviceIds, long timestamp) {
		this.startingTime = timestamp;
		this.deviceLogs.clear();

		for (String id : deviceIds)
			deviceLogs.put(id, null);
	}

	@Override
	public void addLog(String deviceId, long timestamp) {
		Log log = new Log(deviceId, startingTime, timestamp);
		deviceLogs.put(deviceId, log);
	}

	@Override
	public boolean allFinished() {
		// since the map is initialized with (deviceId, null) we check if in the
		// map there are null values left. Dirty, but fast and easy ;).
		return !deviceLogs.values().contains(null);
	}

	@Override
	public List<Log> getLogs() {
		List<Log> temp = new ArrayList<Log>(deviceLogs.values());
		List<Log> ret = new ArrayList<Log>();
		// remove null values
		for (Log log : temp) {
			if (log != null)
				ret.add(log);
		}
		return ret;
	}

	@Override
	public Statistic getStatistic() {
		Statistic ret = new Statistic(startingTime, getLogs());
		ret.calculateFinish();
		return ret;
	}

	public long getStartingTime() {
		return startingTime;
	}

	public void setStartingTime(long startingTime) {
		this.startingTime = startingTime;
	}

	public Map<String, Log> getDeviceLogs() {
		return deviceLogs;
	}

	public void setDeviceLogs(Map<String, Log> deviceLog) {
		this.deviceLogs = deviceLog;
	}

	public static void main(String[] args) {
		Map<String, Log> deviceLogs = Collections
				.synchronizedMap(new HashMap<String, Log>());

		System.out.println(deviceLogs.values().contains(null));

		deviceLogs.put("id1", null);

		System.out.println(deviceLogs.values().contains(null));
	}

}
