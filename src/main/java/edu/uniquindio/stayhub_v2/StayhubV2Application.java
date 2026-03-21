package edu.uniquindio.stayhub_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class StayhubV2Application {

	 static void main(String[] args) {
		 SpringApplication.run(StayhubV2Application.class, args);
	}

}
