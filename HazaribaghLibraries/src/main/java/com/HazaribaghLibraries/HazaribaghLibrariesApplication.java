package com.HazaribaghLibraries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class HazaribaghLibrariesApplication {

	// It's a best practice to use a logger instead of System.out.println
	private static final Logger log = LoggerFactory.getLogger(HazaribaghLibrariesApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(HazaribaghLibrariesApplication.class, args);
		log.info("Application is running");
	}

}
