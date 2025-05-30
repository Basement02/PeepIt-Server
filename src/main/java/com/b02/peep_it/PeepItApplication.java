package com.b02.peep_it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
public class PeepItApplication {

	public static void main(String[] args) {
		// 애플리케이션 전역 기본 시간대 Asia/Seoul
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(PeepItApplication.class, args);
	}
}