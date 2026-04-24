package br.com.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.clinica")
public class ClinicaMedicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicaMedicaApplication.class, args);
	}

}
