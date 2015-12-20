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
package at.ac.tuwien.infosys;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

import at.ac.tuwien.infosys.model.Device;
import at.ac.tuwien.infosys.model.Profile;
import at.ac.tuwien.infosys.store.IDeviceStore;
import at.ac.tuwien.infosys.store.Image;
import at.ac.tuwien.infosys.store.model.DeviceDTO;
import at.ac.tuwien.infosys.store.model.DevicesDTO;

@RestController
@RequestMapping("/device-manager")
public class DeviceManager {

	public static Logger logger = Logger.getLogger(DeviceManager.class
			.getName());

	@Autowired
	private IDeviceStore deviceStore;

	@Autowired
	private AsyncRestTemplate asyncRestTemplate;
	
	/**
	 * This method is used by SD gateway to report its current profile to the
	 * Manager. It is used to simulate POST with GET to support the device.
	 * 
	 * @param msg
	 * @return
	 */
	@RequestMapping(value = "/profile-test/{msg}", method = RequestMethod.GET)
	public ResponseEntity<String> ping(@PathVariable String msg) {
		logger.info("Device profile is : " + msg);

		return new ResponseEntity<String>("Received: " + msg, HttpStatus.OK);
	}

	/**
	 * Adds a profile for the given device.
	 * 
	 * @param id
	 * @param profile
	 * @return
	 */
	@RequestMapping(value = "/profile/{id}", method = RequestMethod.POST)
	public ResponseEntity<String> addProfile(@PathVariable String id,
			@RequestBody String profile) {

		logger.info("Received profile for device: " + id + " : " + profile);

		if (!deviceStore.hasDevice(id)) {
			logger.info("Device for given id " + id + " not known!");

			deviceStore.addDevice(id);

			logger.info("Device for given id " + id + " stored!");
		}

		deviceStore.addProfile(id, profile);

		return new ResponseEntity<String>("Profile added for: " + id,
				HttpStatus.OK);
	}

	/**
	 * Adds a ipAddress for the given device.
	 * 
	 * @param id
	 * @param ipAddress
	 * @return
	 */
	@RequestMapping(value = "/registerIP/{id}", method = RequestMethod.POST)
	public ResponseEntity<String> addIpAddress(@PathVariable String id,
			@RequestBody String ipAddress) {

		logger.info("Received ipAddress for device: " + id + " : " + ipAddress);

		if (!deviceStore.hasDevice(id)) {
			logger.info("Device for given id " + id + " not known!");

			deviceStore.addDevice(id);

			logger.info("Device for given id " + id + " stored!");
		}

		deviceStore.addIpAddress(id, ipAddress);

		return new ResponseEntity<String>("IpAddress added for: " + id,
				HttpStatus.OK);
	}

	@RequestMapping(value = "/registerMeta/{id}/{metaInfo}", method = RequestMethod.GET)
	public ResponseEntity<String> addMetaInfo(@PathVariable String id,
			@PathVariable String metaInfo) {
		logger.info("Received meta information for device: " + id + " : "
				+ metaInfo);
		if (!deviceStore.hasDevice(id)) {
			deviceStore.addDevice(id);
		}
		deviceStore.addDeviceMetaInfo(id, metaInfo);
		return new ResponseEntity<String>("Meta Info added for: " + id,
				HttpStatus.OK);
	}

	@RequestMapping(value = "/profile/{id}", method = RequestMethod.GET)
	public ResponseEntity<Profile> getProfile(@PathVariable String id) {
		logger.info("Get profile for device: " + id);

		if (!deviceStore.hasDevice(id)) {
			logger.info("Device for given id " + id + " not known!");
			return new ResponseEntity<Profile>(HttpStatus.BAD_REQUEST);
		}

		Profile ret = deviceStore.getProfile(id);
		return new ResponseEntity<Profile>(ret, HttpStatus.OK);
	}

	// Gateway sends profile as part of (file- or url-parameter) update request!
	/**
	 * When invoked by a device, e.g., gateway, provides the new sd-image, if
	 * available.
	 */
	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getUpdate(@PathVariable String id) {

		if (!deviceStore.hasDevice(id)) {
			logger.info("Device for given id " + id + " not known!");

			deviceStore.addDevice(id);

			logger.info("Device for given id " + id + " stored!");
			return new ResponseEntity<byte[]>("Nothing to update!".getBytes(),
					HttpStatus.OK);
		}
		
		return new ResponseEntity<byte[]>("Nothing to update!".getBytes(),
				HttpStatus.OK);
		//FIXME Disable updates for local installation
//		try {
//			InputStream updateImage = deviceStore.getUpdate(id);
//
//			if (updateImage == null) {
//				logger.info("No update available for device!");
//				return new ResponseEntity<byte[]>(
//						"Nothing to update!".getBytes(), HttpStatus.OK);
//			}
//
//			logger.info("Update available for device!");
//
//			return new ResponseEntity<byte[]>(IOUtils.toByteArray(updateImage),
//					HttpStatus.OK);
//
//		} catch (IOException e) {
//			logger.info("No update available for device!");
//			return new ResponseEntity<byte[]>("Nothing to update!".getBytes(),
//					HttpStatus.OK);
//		}
	}

	/**
	 * Invoked by a device after a successful update, to notify the manager to
	 * remove the update from the queue.
	 */
	@RequestMapping(value = "/update-successful/{deviceId}/{imageId}", method = RequestMethod.GET)
	public ResponseEntity<String> updateSuccessful(
			@PathVariable String deviceId, @PathVariable String imageId) {

		if (!deviceStore.hasDevice(deviceId)) {
			logger.info("Device for given id " + deviceId + " not known!");
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}

		deviceStore.removeUpdate(deviceId, imageId);

		logger.info("Device successfully finished the update!");

		return new ResponseEntity<String>(
				"Update removed from Manager's queue!", HttpStatus.OK);
	}

	/**
	 * Invoked by the ArtifactBuilder to notify the Manager that a new device
	 * update is available.
	 * 
	 * @param deviceId
	 * @param imageId
	 * @param attachment
	 * @return
	 */
	// TODO get rid of deviceId since Image contains id or set of ids already!
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResponseEntity<String> update(
			@RequestBody Image image) {


		logger.info("Received request to update device(s): "
				+ image.getDeviceIds() + " with image: " + image);

		deviceStore.addUpdate(image);

//		if (force){
			//invoke API manager
			for (String deviceId : image.getDeviceIds()) {
				ListenableFuture<ResponseEntity<String>> result = asyncRestTemplate
						.getForEntity(
								"http://localhost:8080/APIManager/invokeAgent/"+deviceId,String.class);

				result.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
					@Override
					public void onSuccess(ResponseEntity<String> result) {
//						LOGGER.info("Received result from node: "
//								+ result.getBody());
//						// Add to a queue
//						endTS = System.currentTimeMillis();
//						LOGGER.info("Invocation time was: " + startTime + ","
//								+ endTS);
					}

					@Override
					public void onFailure(Throwable t) {
//						LOGGER.info("Error contacting device: "
//								+ t.getMessage());
					}
				});
				
			}
			
//		}
		return new ResponseEntity<String>("Successfully stored image!",
				HttpStatus.OK);
	}

	@RequestMapping(value = "/devices", method = RequestMethod.GET)
	public ResponseEntity<DevicesDTO> getAllDevices() {

		List<Device> devices = deviceStore.getAllDevices();
		// List<DeviceDTO> dtos = new ArrayList<DeviceDTO>();
		DevicesDTO dtos = new DevicesDTO();

		for (Device device : devices) {
			// location=gh1&type=FM5300
			DeviceDTO dto = new DeviceDTO(device.getId(), device.getName(),
					device.getMetaInfo());
			String[] meta = device.getMetaInfo().split("&");
			for (String datum : meta) {
				String[] split = datum.split("=");
				dto.addMetaData(split[0], split[1]);
			}
			dtos.addDTO(dto);
		}
		return new ResponseEntity<DevicesDTO>(dtos, HttpStatus.OK);
	}

	@RequestMapping(value = "/clean", method = RequestMethod.GET)
	public ResponseEntity<String> clean() {
		try {
			deviceStore.clean();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "/unregister/{deviceId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> unregisterDevice(@PathVariable String deviceId) {
		deviceStore.removeDevice(deviceId);
		return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	
	@Configuration
	public static class AsyncRestTemplateFactory {
		@Bean
		public AsyncRestTemplate createAsyncRestTemplate() {
			AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
			return asyncRestTemplate;
		}
	}
}
