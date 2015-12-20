package at.ac.tuwien.infosys.governance.isolatedactuation;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import at.ac.tuwien.infosys.governance.GovernanceScopeManager;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class SimpleCapabilityInvocation extends HystrixCommand<ResponseEntity<String>> {

	private static final Logger LOGGER = Logger
			.getLogger(SimpleCapabilityInvocation.class);
	private final String URL;

//	   G1: GOVERNANCE_SCOPE: QUERY=location=home&owner=stefan||location=x
//							 CONSIDERING_SELECTION_UNCERTAINTY missing_data=location<=-, owner<=+ AND 
//															   decision_treshold=0.8 AND 
//															   selection_strategy=[optimistic|pessimistic|reduct]
//	   STRATEGY magic=true: setProto("mqtt"), updatePollRate ("5s") FOR G1
//							CONSIDERING_ACTUATION_UNCERTAINTY fallBack = [once|must_finish] AND (fallback.isolation.semaphore.maxConcurrentRequests determines how many fall backs are allowed ==retries)
//															  time_to_next_fallback = 500ms AND
//															  run_in_isolation = [true|false] (see https://github.com/Netflix/Hystrix/wiki/How-it-Works#threads--thread-pools) AND
//															  process_id = id AND (Each process gets its own id ie its own thread pool)
//															  tolerate_fault_persentage = 20% AND (this applies for all commands of instanceType=X )
	
	
	public SimpleCapabilityInvocation(String URL) {
		super(HystrixCommandGroupKey.Factory
				.asKey("SimpleCapabilityInvocationGroup"));
		this.URL = URL;
//		HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(3);
//		getProperties().executionTimeoutInMilliseconds().get();
		
		//hystrix.command.SubscriberGetAccount.execution.isolation.thread.timeoutInMilliseconds=566;
	}

	@Override
	protected ResponseEntity<String> run() throws Exception {

		ResponseEntity<String> deviceResponse = new RestTemplate().getForEntity(this.URL, String.class);
		LOGGER.info("Invoked " + this.URL + " and got "+ deviceResponse.getStatusCode());
		
		if (deviceResponse.getStatusCode() != HttpStatus.OK) {
			return new ResponseEntity<String>("Could not reach device.",HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return  deviceResponse;
	}

	
}

/**
 * - The policies are reinforced by the sybl periodically with configurable period
 * - The idempotence of capabilities is crucial here (since we can reinvoke them numerous times to keep the base line)
 * - This is also important for the circuit configuration since the circuit will close automatically  
 * - Each governance process (sybl strategy) get its own thread pool and own Hystrix Commands
 * 
 */

