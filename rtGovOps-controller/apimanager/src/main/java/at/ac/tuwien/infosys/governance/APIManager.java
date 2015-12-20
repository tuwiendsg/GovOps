package at.ac.tuwien.infosys.governance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import at.ac.tuwien.infosys.governance.isolatedactuation.SimpleCapabilityInvocation;
import at.ac.tuwien.infosys.model.Capability;
import at.ac.tuwien.infosys.model.CustomMappingModel;
import at.ac.tuwien.infosys.model.DefaultMappingModel;
import at.ac.tuwien.infosys.model.Result;
import at.ac.tuwien.infosys.monitoring.HystrixMetricsPoller;
import at.ac.tuwien.infosys.monitoring.HystrixMetricsPoller.MetricsAsJsonPollerListener;
import at.ac.tuwien.infosys.proxy.InvocationRegistry;
import at.ac.tuwien.infosys.store.model.DeviceUpdateRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/mapper")
public class APIManager {

	private static final Logger LOGGER = Logger.getLogger(APIManager.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private InvocationRegistry registry;

	@Value("${builder.port}")
	private String builderPort;
	@Value("${builder.context}")
	private String builderContext;
	@Value("${builder.path}")
	private String builderPath;

	/**
	 * Default capability invocation. No custom mapping model required.
	 * 
	 * @param deviceId
	 * @param capaId
	 * @param method
	 * @param args - optional
	 * @return
	 */
	@RequestMapping(value = "/invoke/{deviceId}/{capaId}/{method}", method = RequestMethod.GET)
	public ResponseEntity<String> invokeCapability(
			@PathVariable String deviceId, @PathVariable String capaId,
			@PathVariable String method,
			@RequestParam(value = "args", required = false) String args) {

		
		LOGGER.info("Invoked Mapper: " + deviceId + "/" + capaId + "/" + method);

		String dynamicURL = "";
		// TODO validate call with the repo before invocation
		if (!registry.checkCapability(deviceId, capaId)) {

			Result exist = checkIfexists(deviceId, capaId).getBody();
			LOGGER.info("First check if capability exists " + exist);

			// If capability does not exist in the gateway. Install it!
			//FIXME disable provisioning for local installation			
			if (false && "not found".equals(exist.getCapaResult())) {
				// return new
				// ResponseEntity<String>("No such capability in device",
				// HttpStatus.INTERNAL_SERVER_ERROR);
				LOGGER.info("Capability " + capaId
						+ " not found. Try to install it!");

				// Get assigned manager node
				ResponseEntity<String> balancerResponse = restTemplate
						.getForEntity(
								"http://localhost:8080/SDGBalancer/balancer/resolve/"
										+ deviceId, String.class);
				if (balancerResponse.getStatusCode() != HttpStatus.OK) {
					return new ResponseEntity<String>(
							"Could not reach balancer.",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				String managerIP = balancerResponse.getBody();
				LOGGER.info("Balancer responded with manager IP: " + managerIP);

				// Install capability
				String managerUrl = "http://" + managerIP + ":" + builderPort
						+ builderContext + builderPath;
				List<String> device = new ArrayList<>();
				device.add(deviceId);

				ResponseEntity<String> builderResponse = restTemplate
						.postForEntity(managerUrl,
								new HttpEntity<DeviceUpdateRequest>(
										new DeviceUpdateRequest(device, capaId,
												"1.0.0")), String.class);
				if (builderResponse.getStatusCode() != HttpStatus.ACCEPTED) {
					return new ResponseEntity<String>(
							"Could not install capability",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				LOGGER.info("Builder responded with: "
						+ builderResponse.getBody());

				// Now it is OK to sync invoke the device agent
				String capabilitiyUrl = new DefaultMappingModel(
						"proviagentcapa", deviceId, "install", null)
						.getMapping();
				ResponseEntity<String> deviceResponse = restTemplate
						.getForEntity(capabilitiyUrl, String.class);

				if (deviceResponse.getStatusCode() != HttpStatus.OK) {
					return new ResponseEntity<String>(
							"Device could not install capability",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				LOGGER.info("Device responed with: " + deviceResponse.getBody());

			}

			// register capability
			dynamicURL = new DefaultMappingModel(capaId, deviceId, method, args)
					.getMapping();
			this.registry.addDeviceCapability(deviceId, new Capability(capaId,
					dynamicURL));

		}

		
		dynamicURL = new DefaultMappingModel(capaId, deviceId, method, args)
				.getMapping();

//		ResponseEntity<String> deviceResponse = restTemplate.getForEntity(
//				dynamicURL, String.class);
//		LOGGER.info("Invoked " + dynamicURL + " and got "
//				+ deviceResponse.getStatusCode());
//		if (deviceResponse.getStatusCode() != HttpStatus.OK) {
//			return new ResponseEntity<String>("Could not reach device.",
//					HttpStatus.INTERNAL_SERVER_ERROR);
//		}
		
		ResponseEntity<String> deviceResponse = new SimpleCapabilityInvocation(dynamicURL).execute();
		
		LOGGER.info("Device JSON: " + deviceResponse.getBody());
		return new ResponseEntity<String>(deviceResponse.getBody(),
				HttpStatus.OK);
	}

	/**
	 * Capability invocation with custom mapping model.
	 * 
	 * @param deviceId
	 * @param capaId
	 * @param method
	 * @param args
	 * @return
	 */
	@RequestMapping(value = "/invoke/{deviceId}/{capaId}/{method}", method = RequestMethod.POST)
	public ResponseEntity<String> invokeCapabilityWithcustomMappingModel(
			@PathVariable String deviceId, @PathVariable String capaId,
			@PathVariable String method,
			@RequestParam(value = "args", required = false) String args,
			@RequestBody CustomMappingModel customMappingModel) {

		String dynamicURL = customMappingModel.getMapping();

		ResponseEntity<String> deviceResponse = restTemplate.postForEntity(
				dynamicURL, customMappingModel.getRawModel(), String.class);
		// TODO invoke agent to place mapping model in case it is not available
		// in the repo!
		LOGGER.info("Invoked " + dynamicURL + " and got "
				+ deviceResponse.getStatusCode());
		if (deviceResponse.getStatusCode() != HttpStatus.OK) {
			return new ResponseEntity<String>("Could not reach device.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		LOGGER.info("Device JSON: " + deviceResponse.getBody());
		return new ResponseEntity<String>(deviceResponse.getBody(),
				HttpStatus.OK);
	}

	/**
	 * Utility method to invoke device capability manager to list all device
	 * capabilities.
	 * 
	 * @param deviceId
	 * @return
	 */

	@RequestMapping(value = "/capabilities/list/{deviceId}", method = RequestMethod.GET)
	public ResponseEntity<String> listCapabilities(@PathVariable String deviceId) {

		String baseURL = "http://" + getDeviceURL(deviceId);
		String cManagerPath = "/cgi-bin/mapper/cManager/list";
		// mapp to the cManager
		ResponseEntity<String> deviceResponse = restTemplate.getForEntity(
				baseURL + cManagerPath, String.class);
		LOGGER.info("Invoked Device " + deviceId + " and got "
				+ deviceResponse.getStatusCode());
		if (deviceResponse.getStatusCode() != HttpStatus.OK) {
			return new ResponseEntity<String>("Could not reach device.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		LOGGER.info("Device JSON: " + deviceResponse.getBody());

		return new ResponseEntity<String>(deviceResponse.getBody(),
				HttpStatus.OK);
	}

	/**
	 * Utility method to invoke device capability manager to check if capability
	 * exists.
	 * 
	 * @param deviceId
	 * @return
	 */

	@RequestMapping(value = "/check/{capability}/{deviceId}", method = RequestMethod.GET, consumes = "aplication/json")
	public ResponseEntity<Result> checkIfexists(@PathVariable String deviceId,
			@PathVariable String capability) {

		String baseURL = "http://" + getDeviceURL(deviceId);
		// Example: cManager/capabilities/arguments?arg1=capaTest
		String cManagerPath = "/cgi-bin/mapper/cManager/capabilities/arguments?arg1="
				+ capability;
		ResponseEntity<Result> deviceResponse = restTemplate.getForEntity(
				baseURL + cManagerPath, Result.class);
		LOGGER.info("Invoked Device " + deviceId + " and got "
				+ deviceResponse.getStatusCode());
		if (deviceResponse.getStatusCode() != HttpStatus.OK) {
			return new ResponseEntity<Result>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		Result res = deviceResponse.getBody();
		LOGGER.info("Device JSON: " + res);

		return new ResponseEntity<Result>(res, HttpStatus.OK);
	}

	//TODO FIXME
	@RequestMapping(value = "/invokeAgent/{deviceId}", method = RequestMethod.GET)
	public ResponseEntity<String> invokeAgent(@PathVariable String deviceId) {

		String capabilityUrl = "http://" + getDeviceURL(deviceId)
				+ "/cgi-bin/mapper/proviagentcapa/install";
		ResponseEntity<String> deviceResponse = restTemplate.getForEntity(
				capabilityUrl, String.class);

		if (deviceResponse.getStatusCode() != HttpStatus.OK) {
			return new ResponseEntity<String>("Installation failed on device!",
					HttpStatus.OK);
		}

		LOGGER.info("Device responed with: " + deviceResponse.getBody());
		return new ResponseEntity<String>("Successfully instaled on device!",
				HttpStatus.OK);
	}

	@RequestMapping(value = "/removeDevice/{deviceId}", method = RequestMethod.GET)
	public ResponseEntity<String> removeDevice(@PathVariable String deviceId) {

		LOGGER.info("Trying to delete device: " + deviceId);
		try {
			restTemplate.delete(new URI(
					"http://127.0.0.1:8080/SDGManager/device-manager/unregister/"
							+ deviceId + "/"));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// delete from registry
		registry.removeDevice(deviceId);
		return new ResponseEntity<String>("Successfully deleted device: "
				+ deviceId, HttpStatus.OK);
	}

	
	private String getDeviceURL(String deviceId) {

		return deviceId.replace("_", ".");
	}

	@SuppressWarnings("unused")
	private static <T> T fromJSON(final TypeReference<T> type,
			final String jsonPacket) {
		T data = null;

		try {
			data = new ObjectMapper().readValue(jsonPacket, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
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
