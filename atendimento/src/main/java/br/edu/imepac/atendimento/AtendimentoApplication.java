package br.edu.imepac.atendimento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// escopo restrito ao dominio atendimento: atendimento/prontuario/anotacao/exame neste DataSource
// @EnableScheduling liga o OutboxScheduler que entrega os eventos de notificacao com retry
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "br.edu.imepac.atendimento",
        "br.edu.imepac.commons.config",
        "br.edu.imepac.commons.services.atendimento"
})
@EntityScan(basePackages = "br.edu.imepac.commons.entities.atendimento")
@EnableJpaRepositories(basePackages = "br.edu.imepac.commons.repositories.atendimento")
public class AtendimentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtendimentoApplication.class, args);
    }
}
