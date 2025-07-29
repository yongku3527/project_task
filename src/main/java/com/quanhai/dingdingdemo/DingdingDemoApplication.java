package com.quanhai.dingdingdemo;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DingdingDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DingdingDemoApplication.class, args);


	}

}
