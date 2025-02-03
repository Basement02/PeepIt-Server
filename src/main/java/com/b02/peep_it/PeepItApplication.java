package com.b02.peep_it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.b02.peep_it.common.security")
public class PeepItApplication {

	public static void main(String[] args) {
		SpringApplication.run(PeepItApplication.class, args);
	}

}
