package com.demo.hystrixsentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@SpringBootApplication
@ServletComponentScan
//开启hystrix
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
public class HystrixSentinelApplication {

	public static void main(String[] args) {
		SpringApplication.run(HystrixSentinelApplication.class, args);
	}

}
