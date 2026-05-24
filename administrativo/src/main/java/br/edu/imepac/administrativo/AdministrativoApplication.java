package br.edu.imepac.administrativo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// escopo restrito ao dominio administrativo: evita carregar entidades/repos/services
// de outros bancos (que criariam tabelas erradas neste DataSource)
@SpringBootApplication(scanBasePackages = {
        "br.edu.imepac.administrativo",
        "br.edu.imepac.commons.config",
        "br.edu.imepac.commons.services.administrativo"
})
@EntityScan(basePackages = "br.edu.imepac.commons.entities.administrativo")
@EnableJpaRepositories(basePackages = "br.edu.imepac.commons.repositories.administrativo")
@EnableJpaAuditing
public class AdministrativoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdministrativoApplication.class, args);
    }
}
