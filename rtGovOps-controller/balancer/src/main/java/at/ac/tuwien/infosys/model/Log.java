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

public class Log {

	private String deviceId;
	private long started;
	private long finished;

	public Log() {
		super();
	}

	public Log(String deviceId, long started, long finished) {
		super();
		this.deviceId = deviceId;
		this.started = started;
		this.finished = finished;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public long getStarted() {
		return started;
	}

	public void setStarted(long started) {
		this.started = started;
	}

	public long getFinished() {
		return finished;
	}

	public void setFinished(long finished) {
		this.finished = finished;
	}

	@Override
	public String toString() {
		return "Log [deviceId=" + deviceId + ", started=" + started
				+ ", finished=" + finished + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((deviceId == null) ? 0 : deviceId.hashCode());
		result = prime * result + (int) (finished ^ (finished >>> 32));
		result = prime * result + (int) (started ^ (started >>> 32));
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
		Log other = (Log) obj;
		if (deviceId == null) {
			if (other.deviceId != null)
				return false;
		} else if (!deviceId.equals(other.deviceId))
			return false;
		if (finished != other.finished)
			return false;
		if (started != other.started)
			return false;
		return true;
	}

}
