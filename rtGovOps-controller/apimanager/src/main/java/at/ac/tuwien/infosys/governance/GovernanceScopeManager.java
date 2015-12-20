package at.ac.tuwien.infosys.governance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
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
import org.springframework.web.client.RestTemplate;

import at.ac.tuwien.infosys.RoughScopeManager;
import at.ac.tuwien.infosys.RoughScopeManager.Block;
import at.ac.tuwien.infosys.governance.isolatedactuation.ProcessCapabilityInvocation;
import at.ac.tuwien.infosys.model.DefaultMappingModel;
import at.ac.tuwien.infosys.proxy.ProcessContext;
import at.ac.tuwien.infosys.proxy.ProcessContext.Context;
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
	@Autowired
	private ProcessContext processContext;
	volatile long endTS = 0;

	/*
	curl -H "Content-Type: application/json" -X POST -d "{\"fallback\":\"2\",\"cache_enabled\":\"false\"}"
	http://localhost:8080/APIManager/governanceScope/setProcessProps/testProcess
	
	*/
	@RequestMapping(value = "/setProcessProps/{procId}", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<String> setInstanceProperties(
			@PathVariable String procId, @RequestBody String propertiesJson) {

		LOGGER.info("Received properties for process " + procId + "\n"
				+ propertiesJson);
		this.processContext.registerIfAbsent(procId).setProperties(
				propertiesJson);
		this.processContext.registerIfAbsent(procId).refreshConfig();
		return new ResponseEntity<String>("Sucessfully updated configuration",
				HttpStatus.OK);
		
	
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/globalScope", method = RequestMethod.GET)
	public ResponseEntity<DevicesDTO> getGlobalScope() {
		DevicesDTO devices = new DevicesDTO();
		ResponseEntity<List> balancerResponse = restTemplate.getForEntity(
				"http://localhost:8080/SDGBalancer/balancer/nodes", List.class);
		if (balancerResponse.getStatusCode() != HttpStatus.OK) {
			return new ResponseEntity<DevicesDTO>(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		List<String> runningNodes = (List<String>) balancerResponse.getBody();
		for (String node : runningNodes) {
			LOGGER.info("Get devies from node " + node);
			ResponseEntity<DevicesDTO> managerResponse = restTemplate
					.getForEntity("http://" + node
							+ ":8080/SDGManager/device-manager/devices",
							DevicesDTO.class);

			if (managerResponse.getStatusCode() != HttpStatus.OK)
				return new ResponseEntity<DevicesDTO>(
						HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.info("Node " + node + " has returned ["
					+ managerResponse.getBody().getDevices() + "]");
			devices.getDevices().addAll(managerResponse.getBody().getDevices());
		}

		return new ResponseEntity<DevicesDTO>(devices, HttpStatus.OK);
	}

	// TODO: Change this to post - scope=query - uncertainty info will be passed as Json in the body
	/*
	curl -H "Content-Type: application/json" -X POST -d "{\"missing_data\":\"location=+"&"owner=+"&"type=+\", }"
	http://localhost:8080/APIManager/governanceScope/invokeScope/testProces/owner=stefan"&"location=home/cManager/list/
	 */
	@RequestMapping(value = "/invokeScope/{procId}/{query}/{capaId}/{method}", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<String> invokeCapabilityOnScope(
			@PathVariable String procId, @PathVariable String query,
			@PathVariable String capaId, @PathVariable String method,
			@RequestParam(value = "args", required = false) String args,
			@RequestBody String uncertaintiyPropsJson) {

		// List<DeviceDTO> governanceScope = devices
		// .stream()
		// .filter(d -> d.getMeta().containsKey(scopeMeta[0])
		// && d.getMeta().get(scopeMeta[0]).equals(scopeMeta[1]))
		// .collect(Collectors.toList());

		RoughScopeManager rgsm = new RoughScopeManager();
		List<DeviceDTO> globalScope =  getGlobalScope().getBody().getDevices();
		LOGGER.info("Starting frrom global scope size="+globalScope.size());
		
		Map<String, Object> propertiesMap = JsonParserFactory.getJsonParser().parseMap(uncertaintiyPropsJson);
		String replacement = (((String) propertiesMap.getOrDefault("missing_data","")));
		String selectionStrategy = (((String) propertiesMap.getOrDefault("selection_strategy","")));
	//	List<String>  attrs = (((List<String>) propertiesMap.getOrDefault("attributes",new ArrayList<String>())));
		LOGGER.info(String.format("Got uncertainty config replacement=%s selectionStrategy=%s", 
				replacement, selectionStrategy));
//		
//		if (attrs.size() == 0 ){
//			throw new IllegalArgumentException("Attributes list cannot be empty.");
//		}
		
		List<DeviceDTO> targetScope = rgsm.getDeviceDTOsForORQuery(globalScope, query);
		LOGGER.info(String.format("Got targetscope_size=%d from globalScope_size=%d with query=%s", targetScope.size(),globalScope.size(),query));
		List<DeviceDTO> devicesNoMissing = rgsm.prepareData(targetScope, replacement);
		Map<Block, List<DeviceDTO>> blocks = rgsm.makeBlocks(devicesNoMissing);
		LOGGER.info("Calculated blocks size="+blocks.size());
		
		Map<Block, List<DeviceDTO>> processedBblocks = rgsm.handleSpecialValues(blocks, devicesNoMissing);
		LOGGER.info("Calculated processed blocks "+processedBblocks);
		
		//FIXME Attributes based only on the first element
		List<String> attributes = Arrays.asList(globalScope.get(0).getMeta().keySet().toArray(new String[0]));
//		attributes.add("owner");
//		attributes.add("location");
//		attributes.add("type");
		Set<DeviceDTO> governanceScope = null;
		if ("pessimistic".equals(selectionStrategy)) {
			governanceScope = rgsm.buildLowerApproximationOptimized(
					targetScope, globalScope, attributes, processedBblocks);
		}else{
			governanceScope = rgsm.buildUpperApproximation(targetScope, globalScope,attributes, processedBblocks);
		}

		String response = "Invoking " + governanceScope.size()
				+ " devices ...<br/><br/>";
		List<Future<ResponseEntity<String>>> invocationResults = new ArrayList<>();
		for (DeviceDTO deviceDTO : governanceScope) {
			String dynamicURL = new DefaultMappingModel(capaId,
					deviceDTO.getId(), method, args).getMapping();
			Context procC = this.processContext.registerIfAbsent(procId);
			Future<ResponseEntity<String>> r = new ProcessCapabilityInvocation(
					dynamicURL, procC).queue();
			invocationResults.add(r);
		}
		LOGGER.info("Waiting for " + invocationResults.size() + " results!");
		for (Future<ResponseEntity<String>> result : invocationResults) {
			try {
				String resultBody = result.get().getBody();
				response += resultBody + "<br/><br/>";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<String>(response, HttpStatus.OK);
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
