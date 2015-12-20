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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.infosys.model.Device;
import at.ac.tuwien.infosys.model.DeviceUpdate;
import at.ac.tuwien.infosys.model.Profile;
import at.ac.tuwien.infosys.model.repository.DeviceRepository;
import at.ac.tuwien.infosys.model.repository.DeviceUpdateRepository;

@Component
@Scope(value = "singleton")
public class DeviceStore implements IDeviceStore {

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private DeviceUpdateRepository deviceUpdateRepository;

	@Autowired
	private ImageStorage updateStorage;

	private DeviceStore() {
	}

	public synchronized List<Device> getAllDevices() {
		return deviceRepository.findAll();
	}

	@Transactional
	public synchronized void addDevice(String deviceId) {
		deviceRepository.saveAndFlush(new Device(deviceId, "Gateway-"
				+ deviceId));
	}

	public synchronized long getDeviceCount() {
		return deviceRepository.count();
	}

	public synchronized boolean hasDevice(String id) {
		return deviceRepository.exists(id);
	}

	@Transactional
	public synchronized void addProfile(String id, String profile) {
		if (hasDevice(id)) {
			Device device = getDevice(id);
			device.setProfile(profile);
			deviceRepository.saveAndFlush(device);
		}
	}

	@Transactional
	public synchronized Profile getProfile(String id) {
		Profile ret = null;
		if (hasDevice(id)) {
			ret = deviceRepository.getOne(id).produceProfile();
		}

		return ret;

	}

	@Transactional
	public void addIpAddress(String id, String ipAddress) {
		if (hasDevice(id)) {
			Device device = getDevice(id);
			device.setIpAddress(ipAddress);
			deviceRepository.saveAndFlush(device);
		}
	}

	@Transactional
	public void addDeviceMetaInfo(String id, String metaInfo) {
		if (hasDevice(id)) {
			Device device = getDevice(id);
			device.setMetaInfo(metaInfo);
			deviceRepository.saveAndFlush(device);
		}
	}

	@Transactional
	public synchronized Device getDevice(String id) {
		return deviceRepository.getOne(id);
	}

	@Transactional
	public synchronized void addUpdate(Image image) {

		DeviceUpdate deviceUpdate = null;
		if (deviceUpdateRepository.exists(image.getImageId()))
			deviceUpdate = deviceUpdateRepository.getOne(image.getImageId());

		if (deviceUpdate == null)
			deviceUpdate = new DeviceUpdate(image);

		for (String deviceId : image.getDeviceIds()) {

			if (!hasDevice(deviceId))
				addDevice(deviceId);

			Device device = getDevice(deviceId);

			if (device != null) {
				device.addUpdate(deviceUpdate);
			}
		}
	}

	@Transactional
	public synchronized InputStream getUpdate(String deviceId)
			throws IOException {

		Device device = getDevice(deviceId);

		if (device != null) {
			DeviceUpdate update = device.getUpdate();
			if (update != null)
				return updateStorage.getUpdate(new Image(null, update
						.getImageId(), update.getStorageFileName(), update
						.getStoragePath()));
			else
				return null;
		}

		return null;
	}

	@Transactional
	public synchronized void removeUpdate(String deviceId, String imageId) {
		Device device = getDevice(deviceId);

		if (device != null) {
			DeviceUpdate update = device.removeUpdate(imageId);
			deviceRepository.saveAndFlush(device);
			if (update != null) {
				// Since we want to provision several devices with the same
				// image don't remove it from storage.

				// try {
				// updateStorage.removeUpdate(new Image(null,
				// update.getImageId(),
				// update.getStorageFileName(), update
				// .getStoragePath()));
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}
		}

	}

	@Transactional
	public synchronized void clean() {
		deviceRepository.deleteAll();
		deviceRepository.flush();
		deviceUpdateRepository.deleteAll();
		deviceRepository.flush();
	}

	@Transactional
	public synchronized void removeDevice(String deviceId) {
		if (hasDevice(deviceId)) {
			Device device = getDevice(deviceId);
			deviceRepository.delete(device);
			deviceRepository.flush();
		}
	}

}
