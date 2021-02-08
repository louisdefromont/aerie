package org.eaa690.aerie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AerieApplication {

	public static void main(String[] args) {
		SpringApplication.run(AerieApplication.class, args);
	}

}
