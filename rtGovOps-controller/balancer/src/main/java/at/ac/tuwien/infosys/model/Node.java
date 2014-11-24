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
package at.ac.tuwien.infosys.model;

import java.util.List;

public class Node {

	private String ip;
	private List<String> devices;
	private boolean started = false;

	public Node(String ip, List<String> devices) {
		super();
		this.ip = ip;
		this.devices = devices;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<String> getDevices() {
		return devices;
	}

	public void setDevices(List<String> devices) {
		this.devices = devices;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public boolean isManagedDevice(String device) {
		return devices.contains(device);
	}

	public int getNumberofManagedDevices() {
		return devices.size();
	}

	public void addDevice(String deviceId) {
		devices.add(deviceId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((devices == null) ? 0 : devices.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + (started ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (devices == null) {
			if (other.devices != null)
				return false;
		} else if (!devices.equals(other.devices))
			return false;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (started != other.started)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Node [ip=" + ip + ", devices=" + devices + ", started="
				+ started + "]";
	}

}
