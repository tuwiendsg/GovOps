package at.ac.tuwien.infosys.governance.isolatedactuation;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import at.ac.tuwien.infosys.proxy.ProcessContext.Context;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;

public class ProcessCapabilityInvocation extends
		HystrixCommand<ResponseEntity<String>> {

	private static final Logger LOGGER = Logger
			.getLogger(ProcessCapabilityInvocation.class);
	private final String URL;
	private final Context context;

	public ProcessCapabilityInvocation(final String URL, final Context context) {
		super(
				Setter.withGroupKey(
						HystrixCommandGroupKey.Factory.asKey(context
								.getThreadPoolKey()))
						.andCommandKey(
								HystrixCommandKey.Factory.asKey(context
										.getComandKey()))
						.andThreadPoolKey(
								HystrixThreadPoolKey.Factory.asKey(context
										.getThreadPoolKey()))
		// .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(context.getFallbackThreadPoolKey()))
		);
		this.URL = URL;
		this.context = context;
		//context.refreshConfig();
	}
	@Override
	protected String getCacheKey() {
		// TODO Auto-generated method stub
		return super.getCacheKey();
	}

	@Override
	protected ResponseEntity<String> run() throws Exception {

		LOGGER.info("Circuit break at %: "
				+ this.getProperties().circuitBreakerErrorThresholdPercentage()
						.get());
		
		//This does not prevent Hystrix to short circuit or timeout me
		ResponseEntity<String> deviceResponse = null;
		if (this.context.isCasheEnabled()){
			deviceResponse = this.context.getCache(URL);
				if (deviceResponse == null){
					//not available in cache
					deviceResponse = new RestTemplate().getForEntity(this.URL, String.class);
					context.registerResult(URL, deviceResponse);
				}
		}else{
			deviceResponse = new RestTemplate()
				.getForEntity(this.URL, String.class);
		}
		return deviceResponse;
	}

	
	@Override
	protected ResponseEntity<String> getFallback() {
		LOGGER.info("Get property "+"hystrix.command."+getCommandKey()+".fallback.retries");
		int retries = ConfigurationManager.getConfigInstance().getInt("hystrix.command."+getCommandKey().name()+".fallback.retries");
		LOGGER.info("Fallingback with "+retries+" retries ...");
		
		if (retries > 0){
			FallBackRetryInvocation fallback = new FallBackRetryInvocation(URL, retries);
			ResponseEntity<String> response = fallback.execute();
			LOGGER.info("I was alive and waiting for the response ....");
			return new ResponseEntity<>("Eddited by "+getCommandKey().name()+"-FALLBACK"+response.getBody(),HttpStatus.OK);
		
		}else
			return super.getFallback();
	}
}
