package br.edu.imepac.atendimento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling liga o OutboxScheduler que entrega os eventos de notificacao com retry
@EnableScheduling
@EnableJpaAuditing
@EnableFeignClients(basePackages = "br.edu.imepac.atendimento.integration")
@SpringBootApplication
public class AtendimentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtendimentoApplication.class, args);
    }
}
