package com.firstclub.fc_membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FcMembershipApplication {

	public static void main(String[] args) {
		SpringApplication.run(FcMembershipApplication.class, args);
	}
}