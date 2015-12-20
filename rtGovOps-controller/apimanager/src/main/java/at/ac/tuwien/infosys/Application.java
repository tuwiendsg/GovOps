package at.ac.tuwien.infosys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import at.ac.tuwien.infosys.monitoring.HystrixMetricsPoller;
import at.ac.tuwien.infosys.monitoring.HystrixMetricsPoller.MetricsAsJsonPollerListener;

@Configuration
@PropertySources(value = { @PropertySource("classpath:apimanager.properties"),
		@PropertySource("classpath:common.properties") })
@ComponentScan
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {

	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	
}
