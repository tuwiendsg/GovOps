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

import java.util.List;

import org.springframework.web.context.request.async.DeferredResult;

import at.ac.tuwien.infosys.model.ProvisionRequest;

public interface INodeManager {

	/**
	 * Resolves the node that is responsible for the given device.
	 * 
	 * @param deviceId
	 * @return
	 */
	public String resolve(String deviceId);

	/**
	 * Returns the list of running nodes containing the IP address.
	 * 
	 * @return
	 */
	public List<String> getAvailableNodes();
	
	/**
	 * Returns the list of idle nodes containing the IP address.
	 * 
	 * @return
	 */
	public List<String> getAllNodes();

	/**
	 * Assign the given device to a running node. If currently there is no
	 * running node available the manager tries to start an idle node.
	 * 
	 * @param deviceId
	 * @return
	 */
	public DeferredResult<String> scheduleNode(String deviceId);

	/**
	 * Selects handled devices to be provisioned from the currently running
	 * nodes.
	 * 
	 * @param nrOfDevices
	 * @return
	 */
	public List<ProvisionRequest> provision(int nrOfDevices);

	/**
	 * Resets the internal structures to guarantee the same state for each run.
	 */
	public void reset();

}
