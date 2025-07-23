package com.reliance.grievance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GrievanceApplication extends SpringBootServletInitializer {
//	public class GrievanceApplication {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder app) {
		return app.sources(GrievanceApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(GrievanceApplication.class, args);
	}

}
