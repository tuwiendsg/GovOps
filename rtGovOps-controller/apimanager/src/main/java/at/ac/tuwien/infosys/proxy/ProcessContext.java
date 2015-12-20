package at.ac.tuwien.infosys.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.netflix.config.ConfigurationManager;

@Component
@Scope(value = "singleton")
public class ProcessContext {

	private static final Logger LOGGER = Logger.getLogger(ProcessContext.class);

	private ConcurrentHashMap<String, Context> registry = new ConcurrentHashMap<>();

	public ProcessContext() {
	}

	public Context registerIfAbsent(String procId) {
		if (!this.registry.containsKey(procId)) {
			registry.put(procId, new Context(procId));
		}
		return this.registry.get(procId);
	}

	/**
	 * This is the context for a governance process. The context is not thread
	 * safe. Especially setting configurations should be done with care.
	 * 
	 * @author stefan
	 *
	 */
	public static class Context {

//G1: GOVERNANCE_SCOPE: QUERY=location=home&owner=stefan||location=x
//		 CONSIDERING_SELECTION_UNCERTAINTY missing_data=location<=-, owner<=+ AND 
//										   decision_treshold=0.8 AND 
//										   selection_strategy=[optimistic|pessimistic|reduct]
		
//STRATEGY magic=true: setProto("mqtt"), updatePollRate ("5s") FOR G1
//		CONSIDERING_ACTUATION_UNCERTAINTY fallBack = [1..n] AND (fallback.isolation.semaphore.maxConcurrentRequests determines how many fall backs are allowed ==retries)
//										  time_to_next_fallback = 500ms AND
//										  run_in_isolation = [true|false] (see https://github.com/Netflix/Hystrix/wiki/How-it-Works#threads--thread-pools) AND
//										  process_id = id AND (Each process gets its own id ie its own thread pool) AND
//										  degree_parallelism = 200 AND
//										  keep_alive = 5 AND (in minutes)
//										  tolerate_fault_percentage  = 20% AND (this applies for all commands of instanceType=X )


		private final String processId;
		private final String threadPoolKey;
		private final String comandKey;
		private final String fallbackThreadPoolKey;
		private boolean casheEnabled = false;

		// Define default properties
		private String tolerate_fault_percentage  = "50";
		private String volume_per_circuit = "20";
		private String run_in_isolation = "true";
		private String fallback = "0";
		private String time_before_fallback = "1000";
		private String degree_parallelism = "200";
		private String keep_alive = "5";

		// TODO This cache is not invalidated automatically
		private ConcurrentHashMap<String, ResponseEntity<String>> cache = new ConcurrentHashMap<String, ResponseEntity<String>>();

		public Context(final String procId) {
			this.processId = procId;
			this.threadPoolKey = procId + "_ThreadPoolKey";
			this.comandKey = procId + "_CapabilityInvocationKey";
			this.fallbackThreadPoolKey = procId + "__ThreadPoolKey_Fallback";
		}

		public synchronized void registerResult(String key,
				ResponseEntity<String> result) {
			this.cache.put(key, result);
		}

		public synchronized ResponseEntity<String> getCache(String key) {
			return this.cache.get(key);
		}

		public void refreshConfig() {
			LOGGER.info("Set properties via Archaius for command "
					+ String.format("hystrix.command.%s.fallback.retries",
							this.comandKey));
			// circuit configuration
			ConfigurationManager.getConfigInstance().setProperty(
					String.format("hystrix.command.%s.circuitBreaker.enabled",
							this.comandKey), true);
			ConfigurationManager
					.getConfigInstance()
					.setProperty(
							String.format(
									"hystrix.command.%s.circuitBreaker.errorThresholdPercentage",
									this.comandKey),
							this.tolerate_fault_percentage);
			ConfigurationManager
					.getConfigInstance()
					.setProperty(
							String.format(
									"hystrix.command.%s.circuitBreaker.errorThresholdPercentage",
									this.comandKey),
							Integer.valueOf(this.tolerate_fault_percentage));
			ConfigurationManager
					.getConfigInstance()
					.setProperty(
							String.format(
									"hystrix.command.%s.circuitBreaker.requestVolumeThreshold",
									this.comandKey), this.volume_per_circuit);
			// TODO This need to be aligned with reinforcement interval of
			// governance policies
			ConfigurationManager
					.getConfigInstance()
					.setProperty(
							String.format(
									"hystrix.command.%s.circuitBreaker.sleepWindowInMilliseconds",
									this.comandKey), 2000);// when to attempt to
															// close circuit
															// again

			// Fallback config
			ConfigurationManager.getConfigInstance().setProperty(
					String.format("hystrix.command.%s.fallback.enabled",
							this.comandKey), true);
			ConfigurationManager.getConfigInstance().setProperty(
					String.format("hystrix.command.%s.fallback.retries",
							this.comandKey), this.fallback);
			ConfigurationManager
					.getConfigInstance()
					.setProperty(
							String.format(
									"hystrix.command.%s.execution.isolation.thread.timeoutInMilliseconds",
									this.comandKey), this.time_before_fallback);

			// Thread pool configuration see also
			// https://github.com/Netflix/Hystrix/wiki/How-it-Works#benefits-of-thread-pools
			ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.coreSize",this.threadPoolKey), 10);
			// Max concurrent requests per process
			ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.maxQueueSize",this.threadPoolKey), 200);
			ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.queueSizeRejectionThreshold",this.threadPoolKey), this.degree_parallelism);
			ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.keepAliveTimeMinutes",this.threadPoolKey), this.keep_alive);

			// Requests collapsing - this is not applicable to us, because
			// request can only be collapsed if we can combine multiple input
			// parameters of HystrixCommand.run() e.g. List.add(Item i) can be
			// replaced with List.AddAll(List<Item>) and than we can collapse
			// invocations of List.add() to List.addAll(...)

			// HystrixContext and request caching - The context's lifetime =
			// servlet lifetime (This cannot be changed easily since Context
			// uses ThreadLocal variable)
		}

		public void setProperties(String propertiesJson) {
			Map<String, Object> propertiesMap = JsonParserFactory
					.getJsonParser().parseMap(propertiesJson);
			this.tolerate_fault_percentage = (((String) propertiesMap
					.getOrDefault("tolerate_fault_percentage",
							this.tolerate_fault_percentage)));
			LOGGER.info("Set property \"tolerate_fault_persentage\"="
					+ this.tolerate_fault_percentage);
			this.volume_per_circuit = (((String) propertiesMap.getOrDefault(
					"volume_per_circuit", this.volume_per_circuit)));
			LOGGER.info("Set property \"volume_per_circuit\"="
					+ this.volume_per_circuit);
			this.run_in_isolation = (((String) propertiesMap.getOrDefault(
					"run_in_isolation", this.run_in_isolation)));
			LOGGER.info("Set property \"run_in_isolation\"="
					+ this.run_in_isolation);
			this.fallback = (((String) propertiesMap.getOrDefault("fallback",
					this.fallback)));
			LOGGER.info("Set property \"fallback\"=" + this.fallback);
			this.time_before_fallback = (((String) propertiesMap.getOrDefault(
					"time_before_fallback", this.time_before_fallback)));
			LOGGER.info("Set property \"time_before_fallback\"="
					+ this.time_before_fallback);
			this.casheEnabled = new Boolean((String)propertiesMap.getOrDefault(
					"cache_enabled", "false"));					
			LOGGER.info("Set property \"cache_enabled\"="
					+ this.casheEnabled);
			this.degree_parallelism = (((String) propertiesMap
					.getOrDefault("degree_parallelism",
							this.degree_parallelism)));
			LOGGER.info("Set property \"degree_parallelism\"="
					+ this.degree_parallelism);
			this.keep_alive = (((String) propertiesMap
					.getOrDefault("keep_alive",
							this.keep_alive)));
			LOGGER.info("Set property \"keep_alive\"="
					+ this.keep_alive);


		}

		public String getProcessId() {
			return processId;
		}

		public String getThreadPoolKey() {
			return threadPoolKey;
		}

		public String getComandKey() {
			return comandKey;
		}

		public String getFallbackThreadPoolKey() {
			return fallbackThreadPoolKey;
		}
		
		public boolean isCasheEnabled() {
			return casheEnabled;
		}

	}
}
