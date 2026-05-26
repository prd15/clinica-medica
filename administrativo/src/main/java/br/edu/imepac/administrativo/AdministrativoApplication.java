package br.edu.imepac.administrativo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {
        "br.edu.imepac.administrativo",
        "br.edu.imepac.commons.config"
})
@EnableJpaAuditing
public class AdministrativoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdministrativoApplication.class, args);
    }
}
