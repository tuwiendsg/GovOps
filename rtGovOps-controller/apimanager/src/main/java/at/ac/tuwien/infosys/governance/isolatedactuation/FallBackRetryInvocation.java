package at.ac.tuwien.infosys.governance.isolatedactuation;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class FallBackRetryInvocation extends
		HystrixCommand<ResponseEntity<String>> {

	private static final Logger LOGGER = Logger
			.getLogger(FallBackRetryInvocation.class);
	private final String URL;

	private final int MAX_WAIT_INTERVAL = 60000;// will be interrupted
												// afterwards by hystrix
	private final int maxRetries;

	public FallBackRetryInvocation(final String URL, int maxRetries) {
		super(Setter
				.withGroupKey(
						HystrixCommandGroupKey.Factory
								.asKey("FallbackRetryThreadPool"))
				.andCommandKey(
						HystrixCommandKey.Factory
								.asKey("FallbackRetryInvocation"))
				.andThreadPoolKey(
						HystrixThreadPoolKey.Factory
								.asKey("FallbackRetryThreadPool"))
				.andThreadPoolPropertiesDefaults(
						HystrixThreadPoolProperties.Setter().withCoreSize(30)
								.withMaxQueueSize(30)
								.withQueueSizeRejectionThreshold(30)
								.withKeepAliveTimeMinutes(10))
				.andCommandPropertiesDefaults(
						HystrixCommandProperties.Setter()
								.withExecutionTimeoutInMilliseconds(60000)
								.withCircuitBreakerEnabled(false)));
		this.URL = URL;
		this.maxRetries = maxRetries;
	}

	@Override
	protected ResponseEntity<String> run() throws Exception {

		LOGGER.info("Fallback logic invoked ...");
		ListenableFuture<ResponseEntity<String>> deviceResponse = null;
		int retries = 0;
		AtomicBoolean retry = new AtomicBoolean();
		retry.set(true);

		try {
			do {
				long waitTime = Math.min(getWaitTimeExp(retries),
						MAX_WAIT_INTERVAL);
				LOGGER.info("Fallback logic going " + retries);
				// Get the result
				deviceResponse = new AsyncRestTemplate().getForEntity(this.URL,
						String.class);

				deviceResponse
						.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
							@Override
							public void onSuccess(ResponseEntity<String> result) {
								// String deviceResponse = result.getBody();
								retry.set(false);
								FallBackRetryInvocation.LOGGER
										.info("Success in Fallback ...");
							}

							@Override
							public void onFailure(Throwable t) {
								retry.set(true);
								FallBackRetryInvocation.LOGGER
										.info("Failure in Fallback ...");
								t.printStackTrace(System.out);
							}
						});

				// Wait for the result.
				Thread.sleep(waitTime);

			} while (retry.get() && (retries++ < this.maxRetries));

			if (deviceResponse != null)
				return deviceResponse.get();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new ResponseEntity<String>("Unreachable device after " + retries
				+ " retries!", HttpStatus.REQUEST_TIMEOUT);
	}

	@Override
	protected ResponseEntity<String> getFallback() {
		LOGGER.info("Fallback of fallback failed. Giving up!");
		return new ResponseEntity<String>(
				"You are seeing this message because everything else failed.",
				HttpStatus.OK);
	}

	private long getWaitTimeExp(int retryCount) {

		long waitTime = ((long) Math.pow(2, retryCount) * 200L);

		return waitTime;
	}
}
