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

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;

@Entity
public class Device {

	@Id
	private String id;
	private String name;
	@ManyToMany(cascade = CascadeType.ALL)
	@OrderColumn
	private List<DeviceUpdate> updates;
	private String profile;
	private String ipAddress;
	private String metaInfo;

	public Device() {
		super();
	}

	public Device(String id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.profile = "";
		this.ipAddress = "";
		this.updates = new LinkedList<DeviceUpdate>();
		this.metaInfo = "";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isUpdateAvailable() {
		return !this.updates.isEmpty();
	}

	public String getProfile() {
		return profile;
	}

	public Profile produceProfile() {
		return new Profile(name, id, getProfile());
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setMetaInfo(String metaInfo) {
		this.metaInfo = metaInfo;
	}
	
	public String getMetaInfo() {
		return metaInfo;
	}
	
	public List<DeviceUpdate> getUpdates() {
		return updates;
	}

	public void setUpdates(List<DeviceUpdate> updates) {
		this.updates = updates;
	}

	public void addUpdate(DeviceUpdate update) {
		this.updates.add(update);
	}

	public DeviceUpdate getUpdate() {
		if (!this.updates.isEmpty())
			return this.updates.get(0);

		return null;
	}

	public DeviceUpdate removeUpdate(String id) {

		DeviceUpdate toRemove = null;

		synchronized (updates) {
			for (DeviceUpdate tmp : updates) {
				if (tmp.getImageId().equals(id)) {
					toRemove = tmp;
					break;
				}
			}

			this.updates.remove(toRemove);
		}

		return toRemove;
	}

}
