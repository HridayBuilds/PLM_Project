package com.odoo.plm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PlmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlmBackendApplication.class, args);
	}

}
