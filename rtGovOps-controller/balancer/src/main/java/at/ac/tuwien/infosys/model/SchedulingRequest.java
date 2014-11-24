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

import org.springframework.web.context.request.async.DeferredResult;

public class SchedulingRequest {

	private String deviceId;
	private DeferredResult<String> result;

	public SchedulingRequest() {
		super();
	}

	public SchedulingRequest(String deviceId, DeferredResult<String> result) {
		super();
		this.deviceId = deviceId;
		this.result = result;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public DeferredResult<String> getResult() {
		return result;
	}

	public void setResult(DeferredResult<String> result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "SchedulingRequest [deviceId=" + deviceId + ", result=" + result
				+ "]";
	}
}
