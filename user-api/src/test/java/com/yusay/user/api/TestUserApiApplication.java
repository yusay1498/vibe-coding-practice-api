package com.yusay.user.api;

import org.springframework.boot.SpringApplication;

public class TestUserApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(UserApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
