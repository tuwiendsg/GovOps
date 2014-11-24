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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import at.ac.tuwien.infosys.logger.ILogger;
import at.ac.tuwien.infosys.manager.INodeManager;
import at.ac.tuwien.infosys.model.Log;
import at.ac.tuwien.infosys.model.ProvisionRequest;
import at.ac.tuwien.infosys.model.Statistic;
import at.ac.tuwien.infosys.store.model.DeviceUpdateRequest;

@RestController
@RequestMapping("/balancer")
public class Balancer {

	private Logger logger = Logger.getLogger(Balancer.class.getName());

	@Autowired
	private INodeManager nodeManager;

	@Autowired
	private ILogger provisioningLogger;

	@Autowired
	private AsyncRestTemplate asyncRestTemplate;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${builder.port}")
	private String builderPort;
	@Value("${builder.context}")
	private String builderContext;
	@Value("${builder.path}")
	private String builderPath;
	@Value("${builder.clean.path}")
	private String builderCleanPath;

	public Balancer() {
	}

	@RequestMapping(value = "/ip", method = RequestMethod.GET)
	public String getIp() {
		InetAddress ip;
		String hostname;
		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
			return "{\"ip\":\"" + ip + "\", \"hostname\":\"" + hostname + "\"}";
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "Error: " + e.getMessage();
		}
	}

	@RequestMapping(value = "/resolve/{deviceId}", method = RequestMethod.GET)
	public String getNode(@PathVariable String deviceId) {
		return nodeManager.resolve(deviceId);
	}

	@RequestMapping(value = "/assign/{deviceId}", method = RequestMethod.GET)
	public DeferredResult<String> assignNode(@PathVariable String deviceId) {
		return nodeManager.scheduleNode(deviceId);
	}

	@RequestMapping(value = "/nodes", method = RequestMethod.GET)
	public List<String> nodes() {
		return nodeManager.getAvailableNodes();
	}

	@RequestMapping(value = "/provision/{nrOfDevices}/{componentName}/{version}", method = RequestMethod.GET)
	public ResponseEntity<String> startProvisioning(
			@PathVariable Integer nrOfDevices,
			@PathVariable String componentName, @PathVariable String version) {

		logger.info("Start provisioning of " + nrOfDevices + " device(s)!");

		if (nrOfDevices <= 0)
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

		// get List of devices to provision
		List<ProvisionRequest> toProvision = nodeManager.provision(nrOfDevices);

		logger.info("Selected devices to be provisioned: " + toProvision);

		List<String> fullDeviceList = new ArrayList<String>();

		// save time stamp before provisioning is triggered
		long startedProvisiong = System.currentTimeMillis();
		// invoke each node to start provisioning
		logger.info("Start contacting nodes and send provisioning request!");
		for (ProvisionRequest request : toProvision) {

			List<String> deviceIds = request.getDevices();

			String builderUrl = "http://" + request.getNode() + ":"
					+ builderPort + builderContext + builderPath;

			logger.info("Contact node: " + builderUrl + " to provision: "
					+ deviceIds);

			DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
					deviceIds, componentName, version);

			updateRequest.setPush(true);

			// use asynchronous rest-template to not block
			ListenableFuture<ResponseEntity<String>> result = asyncRestTemplate
					.postForEntity(builderUrl,
							new HttpEntity<DeviceUpdateRequest>(
									new DeviceUpdateRequest(deviceIds,
											componentName, version)),
							String.class);
			result.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
				@Override
				public void onSuccess(ResponseEntity<String> result) {
					logger.info("Received result from node: "
							+ result.getBody());
				}

				@Override
				public void onFailure(Throwable t) {
					logger.severe("Error occured while contacting node: "
							+ t.getMessage());
				}
			});

			// add currently provisioned devices to the overall list
			fullDeviceList.addAll(deviceIds);
		}

		logger.info("Finished contacting nodes!");

		// initiate the tracking phase of the provisioning
		provisioningLogger.startLogging(fullDeviceList, startedProvisiong);

		return new ResponseEntity<String>(
				"Successfully triggered provisioning of: "
						+ fullDeviceList.size() + " devices!",
				HttpStatus.ACCEPTED);
	}

	// TODO both methods save the timestamp when they got called, for
	// evaluation. Although the method of reporting the successful update
	// overwrites the last timestamp every time.

	/**
	 * Gets called by the device to report a successful update. Has to store
	 * last timestamp when invoked mulitple times ...
	 * 
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value = "/done/{deviceId}", method = RequestMethod.GET)
	public ResponseEntity<String> reportSuccessfulUpdate(
			@PathVariable String deviceId) {

		logger.info("Device :" + deviceId
				+ " successfully finished provisioning!");

		provisioningLogger.addLog(deviceId, System.currentTimeMillis());

		return new ResponseEntity<String>(HttpStatus.ACCEPTED);
	}

	@RequestMapping(value = "/provisioning/finished", method = RequestMethod.GET)
	public ResponseEntity<String> provisioningFinished() {
		return new ResponseEntity<String>("Provisioning finished?: "
				+ provisioningLogger.allFinished(), HttpStatus.OK);
	}

	@RequestMapping(value = "/provisioning/logs", method = RequestMethod.GET)
	public ResponseEntity<List<Log>> getProvisoningLogs() {
		return new ResponseEntity<List<Log>>(provisioningLogger.getLogs(),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/provisioning/statistic", method = RequestMethod.GET)
	public ResponseEntity<Statistic> getProvisoningStatistic() {
		Statistic statistic = provisioningLogger.getStatistic();
		// TODO find better way to avoid printing all logs;
		statistic.getLogs().clear();
		return new ResponseEntity<Statistic>(statistic, HttpStatus.OK);
	}

	@RequestMapping(value = "/provisioning/clean", method = RequestMethod.GET)
	public ResponseEntity<String> cleanupProvisioning() {

		List<String> runningNodes = nodeManager.getAllNodes();

		for (String node : runningNodes) {

			String builderUrl = "http://" + node + ":" + builderPort
					+ builderContext + builderCleanPath;

			logger.info("Contact node: " + builderUrl + " to clean-up!");

			try {
				ResponseEntity<String> result = restTemplate.getForEntity(
						builderUrl, String.class);

				logger.info("Received response from node: " + builderUrl + " "
						+ result.getStatusCode());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// nodeManager.reset();

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

	@Configuration
	public static class RestTemplateFactory {

		@Bean
		public RestTemplate createRestTemplate() {
			RestTemplate restTemplate = new RestTemplate();
			return restTemplate;
		}
	}

}
