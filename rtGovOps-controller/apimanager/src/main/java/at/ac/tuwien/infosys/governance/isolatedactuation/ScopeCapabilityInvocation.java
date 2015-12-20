package at.ac.tuwien.infosys.governance.isolatedactuation;

import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import at.ac.tuwien.infosys.governance.GovernanceScopeManager;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class ScopeCapabilityInvocation extends HystrixCommand<ResponseEntity<String>> {

	private static final Logger LOGGER = Logger
			.getLogger(ScopeCapabilityInvocation.class);
	private final String URL;


	public ScopeCapabilityInvocation(String URL) {
//		super(HystrixCommandGroupKey.Factory
//				.asKey("ScopeCapabilityInvocationGroup"));
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ScopeCapabilityInvocationGroup"))
				.andCommandKey(HystrixCommandKey.Factory.asKey("ScopeCapability"))
				.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("ScopeCapabilityInvocationGroupThreadPool"))
				.andThreadPoolPropertiesDefaults(
						HystrixThreadPoolProperties.Setter()
							.withCoreSize(30)
							.withMaxQueueSize(30)
							.withQueueSizeRejectionThreshold(10)
						)
	            .andCommandPropertiesDefaults(
	            		HystrixCommandProperties.Setter()
	            			.withExecutionTimeoutInMilliseconds(5000)
	            			
	            		)
	            	// use a different threadpool for the fallback command
                    // so saturating the RemoteServiceX pool won't prevent
                    // fallbacks from executing
                 //.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("ScopeInvocationFallback"))		
				);
		this.URL = URL;
//		HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(3);
//		getProperties().executionTimeoutInMilliseconds().get();
		
		//hystrix.command.SubscriberGetAccount.execution.isolation.thread.timeoutInMilliseconds=566;
	}

	@Override
	protected ResponseEntity<String> run() throws Exception {

		LOGGER.info("Circuit break at %: "+this.getProperties().circuitBreakerErrorThresholdPercentage().get());
		ResponseEntity<String> deviceResponse =  new RestTemplate().getForEntity(this.URL, String.class);
	
		return  deviceResponse;
	}

}
//
//So it would be much useful if system could detect that a service is failing and avoid clients do more requests until some period of time. 
//And this is what circuit breaker does. For each execution check if the circuit is open (tripped) which means that an error has occurred 
//and the request will be not sent to service and fallback logic will be executed. But if the circuit is closed then the request is processed and may work.

//Hystrix maintains an statistical database of number of success request vs failed requests. When Hystrix detects that in a defined spare of 
//time, a threshold of failed commands has reached, it will open the circuit so future request will be able to return the error as soon as possible 
//without having to consume resources to a service which probably is offline. But the good news is that Hystrix is also the responsible of closing the circuit. 
//After elapsed time Hystrix will try to run again an incoming request, if this request is successful, then it will close the circuit and if not it will maintain the circuit opened.