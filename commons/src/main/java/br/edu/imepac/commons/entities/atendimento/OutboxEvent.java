package br.edu.imepac.commons.entities.atendimento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// Padrao Outbox: o evento de integracao e gravado na MESMA transacao do negocio
// (registrar atendimento). Assim a notificacao ao agendamento nunca se perde —
// mesmo que o processo caia entre o commit e o envio, o evento fica aqui para retry.
@Entity
@Table(name = "outbox_event", indexes = {
        // o scheduler busca por status; indice acelera o polling
        @Index(name = "idx_outbox_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // tipo do agregado de origem — ex.: "CONSULTA"
    @Column(nullable = false)
    private String aggregateType;

    // id do agregado — aqui, o consultaId que sera notificado
    @Column(nullable = false)
    private String aggregateId;

    // tipo do evento — ex.: "CONFIRMACAO_REALIZACAO"
    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private int tentativas;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    // so preenchido quando o evento e marcado como PROCESSADO
    private LocalDateTime processadoEm;

    // fabrica de um evento novo, sempre PENDENTE e com zero tentativas
    public static OutboxEvent pendente(String aggregateType, String aggregateId,
                                       String eventType, String payload) {
        OutboxEvent evento = new OutboxEvent();
        evento.aggregateType = aggregateType;
        evento.aggregateId = aggregateId;
        evento.eventType = eventType;
        evento.payload = payload;
        evento.status = OutboxStatus.PENDENTE;
        evento.tentativas = 0;
        evento.criadoEm = LocalDateTime.now();
        evento.processadoEm = null;
        return evento;
    }

    // entrega bem-sucedida: encerra o ciclo de vida
    public void marcarProcessado() {
        this.status = OutboxStatus.PROCESSADO;
        this.processadoEm = LocalDateTime.now();
    }

    // entrega falhou: conta a tentativa e mantem o evento elegivel para novo retry
    public void registrarFalha() {
        this.tentativas++;
        this.status = OutboxStatus.FALHA;
    }
}
