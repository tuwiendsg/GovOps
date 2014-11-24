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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import at.ac.tuwien.infosys.model.Component;
import at.ac.tuwien.infosys.model.Dependency;
import at.ac.tuwien.infosys.model.Plan;
import at.ac.tuwien.infosys.model.Version;
import at.ac.tuwien.infosys.repository.IComponentRepository;
import at.ac.tuwien.infosys.repository.NotFoundException;
import at.ac.tuwien.infosys.store.Image;
import at.ac.tuwien.infosys.store.ImageStorage;
import at.ac.tuwien.infosys.store.model.DeviceUpdateRequest;
import at.ac.tuwien.infosys.util.ImageUtil;

@RestController
@RequestMapping("/artifact-builder")
public class ArtifactBuilder implements IArtifactBuilder {

	private final Logger logger = Logger.getLogger(ArtifactBuilder.class
			.getName());

	@Autowired
	private ImageStorage imageStorage;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ImageUtil imageUtil;

	@Value("${dms.url}")
	private String dmsURL;

	@Value("${manager.url}")
	private String managerURL;

	@Value("${manager.clean.url}")
	private String managerCleanURL;

	@Autowired
	private IComponentRepository componentRepository;

	@RequestMapping(value = "/build", method = RequestMethod.POST)
	public ResponseEntity<String> build(
			@RequestBody DeviceUpdateRequest updateRequest) {

		logger.info("Got request: " + updateRequest);

		if (updateRequest == null)
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

		updateRequest.setVersion(updateRequest.getVersion().replace(".", "_"));

		ResponseEntity<Component[]> dmsResponse = restTemplate.getForEntity(
				dmsURL, Component[].class, updateRequest.getComponent(),
				updateRequest.getVersion());

		logger.info("Invoked Component/Dependency-Management and received: "
				+ dmsResponse.getStatusCode());

		if (dmsResponse.getStatusCode() != HttpStatus.OK)
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);

		Plan bundle = new Plan(dmsResponse.getBody());

		logger.info("Received following bundle: " + bundle);

		try {
			String idPrefix = updateRequest.getComponent() + "_"
					+ updateRequest.getVersion();

			// search if an image for the respective component and version
			// already exists
			Image image = imageStorage.getUpdate(idPrefix);

			if (image == null) {
				Path imagePath = imageUtil.createImage(bundle, idPrefix);

				// Upload image to image store
				image = imageStorage
						.storeUpdate(updateRequest.getDeviceIds(),
								imageUtil.getImageId(),
								Files.newInputStream(imagePath));

				logger.info("Finished uploading image to image-store");
			} else {
				logger.info("Image already present in image-store, reuse it!");
				if (image.getDeviceIds() == null)
					image.setDeviceIds(updateRequest.getDeviceIds());
			}

			// Invoke device manager to send image
//
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("force", updateRequest.isPush());

			ResponseEntity<String> managerResponse = restTemplate
					.postForEntity(managerURL, image, String.class);

			logger.info("Received request from device-manager: "
					+ managerResponse.getStatusCode());

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (imageUtil != null)
				try {
					imageUtil.clean();
				} catch (Exception e) {
				}
		}

		return new ResponseEntity<String>(HttpStatus.ACCEPTED);
	}

	@RequestMapping(value = "/plan/{componentName}/{componentVersion}", method = RequestMethod.GET)
	public ResponseEntity<List<Component>> mockCDM(
			@PathVariable String componentName,
			@PathVariable String componentVersion) {

		List<Component> components = new ArrayList<Component>();

		try {
			Component component = componentRepository.get(componentName,
					new Version(componentVersion));

			for (Dependency dependency : componentRepository
					.getDependencies(component)) {
				components.add(componentRepository.get(dependency.getName(),
						dependency.getVersion()));
			}

			components.add(component);
		} catch (NotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<List<Component>>(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Returning component-list: " + components);

		return new ResponseEntity<List<Component>>(components, HttpStatus.OK);
	}

	@RequestMapping(value = "/clean", method = RequestMethod.GET)
	public ResponseEntity<String> clean() {

		logger.info("Start to clean-up build chain");

		try {
			imageStorage.cleanStorage();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("Finished cleaning image-storage!");

		try {
			imageUtil.clean();
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("Finished cleaning image-util!");

		ResponseEntity<String> managerResponse = restTemplate.getForEntity(
				managerCleanURL, String.class);

		logger.info("Finished forwarding clean to manager and got:"
				+ managerResponse.getStatusCode());

		return new ResponseEntity<String>(HttpStatus.OK);
	}

	@Configuration
	public static class RestTemplateFactory {

		@Bean
		public RestTemplate createRestTemplate() {
			RestTemplate restTemplate = new RestTemplate();
			return restTemplate;
		}
	}

}
