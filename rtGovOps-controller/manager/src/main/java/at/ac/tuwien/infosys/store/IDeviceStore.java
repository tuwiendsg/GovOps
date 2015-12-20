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
package at.ac.tuwien.infosys.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import at.ac.tuwien.infosys.model.Device;
import at.ac.tuwien.infosys.model.Profile;

public interface IDeviceStore {

	public List<Device> getAllDevices();
	public void addDevice(String deviceId);

	public boolean hasDevice(String id);

	public void addProfile(String id, String profile);
	
	public void addIpAddress(String id, String ipAddress);
	
	public void addDeviceMetaInfo(String id, String metaInfo);

	public Profile getProfile(String id);

	// public Device getDevice(String id);

	public void addUpdate(Image image);

	public InputStream getUpdate(String deviceId) throws IOException;

	public void removeUpdate(String deviceId, String imageId);
	
	public void clean();
	public void removeDevice(String id);

}
