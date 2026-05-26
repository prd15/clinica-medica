package br.edu.imepac.agendamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// escopo restrito ao dominio agendamento: so a entidade Consulta neste DataSource
@SpringBootApplication(scanBasePackages = {
        "br.edu.imepac.agendamento",
        "br.edu.imepac.commons.config",
        "br.edu.imepac.commons.services.agendamento"
})
@EntityScan(basePackages = "br.edu.imepac.commons.entities.agendamento")
@EnableJpaRepositories(basePackages = "br.edu.imepac.commons.repositories.agendamento")
@EnableJpaAuditing
public class AgendamentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendamentoApplication.class, args);
    }
}
