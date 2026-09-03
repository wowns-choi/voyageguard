package com.voyageguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // OutboxRelay의 @Scheduled 폴링을 실제로 동작시키기 위해 필요
public class VoyageguardApplication {
	public static void main(String[] args) {
		SpringApplication.run(VoyageguardApplication.class, args);
	}
}
