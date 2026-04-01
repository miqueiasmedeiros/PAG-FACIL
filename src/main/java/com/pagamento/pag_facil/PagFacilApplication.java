package com.pagamento.pag_facil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PagFacilApplication {

	public static void main(String[] args) {
		SpringApplication.run(PagFacilApplication.class, args);
	}

}
