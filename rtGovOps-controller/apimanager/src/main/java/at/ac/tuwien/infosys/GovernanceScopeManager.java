package at.ac.tuwien.infosys;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import at.ac.tuwien.infosys.store.model.DeviceDTO;
import at.ac.tuwien.infosys.store.model.DevicesDTO;

@RestController
@RequestMapping("/governanceScope")
public class GovernanceScopeManager {

	private static final Logger LOGGER = Logger
			.getLogger(GovernanceScopeManager.class);

	@Autowired
	private AsyncRestTemplate asyncRestTemplate;

	@Autowired
	private RestTemplate restTemplate;

	volatile long endTS = 0;

	@RequestMapping(value = "/invokeScope/{scope}/{capaId}/{method}", method = RequestMethod.GET)
	public ResponseEntity<String> invokeCapabilityOnScope(
			@PathVariable String scope, @PathVariable String capaId,
			@PathVariable String method,
			@RequestParam(value = "args", required = false) String args) {

		long startTS = System.currentTimeMillis();

		LOGGER.info("Invoked Mapper on scope : " + scope);
		List<DeviceDTO> devices = getAllDevices().getBody().getDevices();
		LOGGER.info("Found so many devices: " + devices.size());

		String[] scopeMeta = scope.split("=");
		LOGGER.info("Scope key=" + scopeMeta[0] + ", scope value="
				+ scopeMeta[1]);

//		List<DeviceDTO> governanceScope = devices.stream()
//				.filter(d -> d.getName().startsWith(scopeMeta[1]))
//				.collect(Collectors.toList());
		
		List<DeviceDTO> governanceScope = devices
				.stream()
				.filter(d -> d.getMeta().containsKey(scopeMeta[0])
						&& Integer.valueOf(d.getMeta().get(scopeMeta[0])) <= Integer
								.valueOf(scopeMeta[1]))
				.collect(Collectors.toList());

		LOGGER.info("Governance scope includes " + governanceScope.size()
				+ "! " + governanceScope);

		long beforeDeviceInvoceation = System.currentTimeMillis();

		String response = "Invoking " + governanceScope.size() + " devices ...";
		for (DeviceDTO deviceDTO : governanceScope) {
			try {
				ListenableFuture<ResponseEntity<String>> result = asyncRestTemplate
						.getForEntity(
								"http://localhost:8080/APIManager/mapper/invoke/"
										+ deviceDTO.getId() + "/" + capaId
										+ "/" + method, String.class);

				result.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
					@Override
					public void onSuccess(ResponseEntity<String> result) {
						LOGGER.info("Received result from node: "
								+ result.getBody());
						// Add to a queue
						endTS = System.currentTimeMillis();
						LOGGER.info("Invocation time was: " + startTS + ","
								+ +beforeDeviceInvoceation + ", " + endTS);
					}

					@Override
					public void onFailure(Throwable t) {
						LOGGER.info("Error contacting device: "
								+ t.getMessage());
					}
				});

				// ResponseEntity<String> res = invokeCapability(
				// deviceDTO.getId(), capaId, method, args);
				// response += res.getBody() + "/n </br> ";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new ResponseEntity<String>(response, HttpStatus.OK);
//		return new ResponseEntity<>("NOT SUPPORDED with JAVA 1.7",
//				HttpStatus.HTTP_VERSION_NOT_SUPPORTED);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/globalScope", method = RequestMethod.GET)
	public ResponseEntity<DevicesDTO> getAllDevices() {

		DevicesDTO devices = new DevicesDTO();
		// List<DeviceDTO> devices = new ArrayList<DeviceDTO>();

		ResponseEntity<List> balancerResponse = restTemplate.getForEntity(
				"http://128.130.172.231:8080/SDGBalancer/balancer/nodes",
				List.class);
		if (balancerResponse.getStatusCode() != HttpStatus.OK) {
			return new ResponseEntity<DevicesDTO>(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		List<String> runningNodes = (List<String>) balancerResponse.getBody();

		for (String node : runningNodes) {
			LOGGER.info("Get devies from node " + node);
			// Since Spring <4 is retarded we have to either create DEVICES
			// DTO of upgrade to Spring 4
			ResponseEntity<DevicesDTO> managerResponse = restTemplate
					.getForEntity("http://" + node
							+ ":8080/SDGManager/device-manager/devices",
							DevicesDTO.class);

			if (managerResponse.getStatusCode() != HttpStatus.OK)
				return new ResponseEntity<DevicesDTO>(
						HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.info("Node " + node + " has returned ["
					+ managerResponse.getBody().getDevices() + "]");
			// List<DeviceDTO> nodesDevices = fromJSON(new
			// TypeReference<List<DeviceDTO>>() {}, managerResponse.getBody());
			devices.getDevices().addAll(managerResponse.getBody().getDevices());
		}

		// ResponseEntity<DevicesDTO> managerResponse = restTemplate
		// .getForEntity(
		// "http://128.130.172.174:8080/SDGManager/device-manager/devices",
		// DevicesDTO.class);
		// devices.addAll(managerResponse.getBody().getDevices());

		// String devicesString = "{Devices: ";
		// for (DeviceDTO device : devices) {
		// devicesString += device.toString() + ", ";
		// }
		// devicesString += "}";
		// LOGGER.info("Found devices:  " + devices);

		return new ResponseEntity<DevicesDTO>(devices, HttpStatus.OK);
	}

	@RequestMapping(value = "/checkDevices/{scopeSize}/{capaId}", method = RequestMethod.GET)
	public ResponseEntity<String> getCheckDevices(
			@PathVariable String scopeSize, @PathVariable String capaId) {

		final long startTime = System.currentTimeMillis();

		Integer size = Integer.valueOf(scopeSize);
		// List<DeviceDTO> devices = getAllDevices().getBody().getDevices();
		ResponseEntity<DevicesDTO> managerResponse = restTemplate.getForEntity(
				"http://" + "128.130.172.174"
						+ ":8080/SDGManager/device-manager/devices",
				DevicesDTO.class);

		List<DeviceDTO> governanceScope = new ArrayList<DeviceDTO>();

		for (int i = 0; i < size; i++) {

			governanceScope.add(managerResponse.getBody().getDevices().get(i));
		}

		for (DeviceDTO deviceDTO : governanceScope) {
			// TODO think how to reuse tomcat thread pool
			try {
				ListenableFuture<ResponseEntity<String>> result = asyncRestTemplate
						.getForEntity(
								"http://localhost:8080/APIManager/mapper/check/"
										+ capaId + "/" + deviceDTO.getId(),
								String.class);

				result.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
					@Override
					public void onSuccess(ResponseEntity<String> result) {
						LOGGER.info("Received result from node: "
								+ result.getBody());
						// Add to a queue
						endTS = System.currentTimeMillis();
						LOGGER.info("Invocation time was: " + startTime + ","
								+ endTS);
					}

					@Override
					public void onFailure(Throwable t) {
						LOGGER.info("Error contacting device: "
								+ t.getMessage());
					}
				});

				// ResponseEntity<String> res = invokeCapability(
				// deviceDTO.getId(), capaId, method, args);
				// response += res.getBody() + "/n </br> ";
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return new ResponseEntity<String>("Send checking invocations!",
				HttpStatus.OK);
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
