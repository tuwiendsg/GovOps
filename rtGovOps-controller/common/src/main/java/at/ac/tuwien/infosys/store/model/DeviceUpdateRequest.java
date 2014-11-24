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
package at.ac.tuwien.infosys.store.model;

import java.util.List;

public class DeviceUpdateRequest {

	private List<String> deviceIds;
	private String component;
	private String version;
	private boolean push = false;

	public DeviceUpdateRequest() {
		super();
	}

	public DeviceUpdateRequest(List<String> deviceIds, String component,
			String version) {
		super();
		this.deviceIds = deviceIds;
		this.component = component;
		this.version = version;
	}

	public List<String> getDeviceIds() {
		return deviceIds;
	}

	public void setDeviceIds(List<String> deviceIds) {
		this.deviceIds = deviceIds;
	}

	public String getComponent() {
		return component;
	}

	public void setComponents(String component) {
		this.component = component;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isPush() {
		return push;
	}

	public void setPush(boolean push) {
		this.push = push;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((component == null) ? 0 : component.hashCode());
		result = prime * result
				+ ((deviceIds == null) ? 0 : deviceIds.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		DeviceUpdateRequest other = (DeviceUpdateRequest) obj;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
			return false;
		if (deviceIds == null) {
			if (other.deviceIds != null)
				return false;
		} else if (!deviceIds.equals(other.deviceIds))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeviceUpdateRequest [deviceIds=" + deviceIds + ", component="
				+ component + ", version=" + version + "]";
	}

}
