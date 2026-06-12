package br.edu.imepac.administrativo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients(basePackages = "br.edu.imepac.administrativo.integration")
public class AdministrativoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdministrativoApplication.class, args);
    }
}
