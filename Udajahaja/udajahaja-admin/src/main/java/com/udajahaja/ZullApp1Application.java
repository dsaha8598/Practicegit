package com.udajahaja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ZullApp1Application {

	public static void main(String[] args) {
		SpringApplication.run(ZullApp1Application.class, args);
	}

}
