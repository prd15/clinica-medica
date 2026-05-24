package br.edu.imepac.commons.entities.atendimento;

import br.edu.imepac.commons.entities.BaseEntity;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Padrao Outbox: o evento de integracao e gravado na MESMA transacao do negocio
// (registrar atendimento). Assim a notificacao ao agendamento nunca se perde —
// mesmo que o processo caia entre o commit e o envio, o evento fica aqui para retry.
//
// Estende BaseEntity — usa createdAt (auditoria automatica) como timestamp de criacao
// do evento. processadoEm e marcado no estado terminal (PROCESSADO/DESCARTADO).
@Entity
@Table(name = "outbox_event", indexes = {
        // o scheduler busca por status+tentativas; indice composto evita full scan
        // quando a tabela crescer com eventos PROCESSADO/DESCARTADO
        @Index(name = "idx_outbox_status_tentativas", columnList = "status, tentativas")
})
@Data
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // so preenchido quando o evento atinge estado terminal (PROCESSADO ou DESCARTADO)
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
        evento.processadoEm = null;
        return evento;
    }

    // entrega bem-sucedida: encerra o ciclo de vida
    public void marcarProcessado() {
        this.status = OutboxStatus.PROCESSADO;
        this.processadoEm = LocalDateTime.now();
    }

    // entrega falhou por erro transitorio. conta a tentativa; se esgotou o limite,
    // promove para DESCARTADO (terminal). Senao mantem FALHA (elegivel para retry).
    public void registrarFalha(int maxRetry) {
        this.tentativas++;
        if (this.tentativas >= maxRetry) {
            this.status = OutboxStatus.DESCARTADO;
            this.processadoEm = LocalDateTime.now();
        } else {
            this.status = OutboxStatus.FALHA;
        }
    }

    // descarte imediato por erro permanente (404, 409, payload invalido, tipo desconhecido).
    // nao incrementa tentativas — vai direto pro terminal porque retry nao resolve.
    public void descartar() {
        this.status = OutboxStatus.DESCARTADO;
        this.processadoEm = LocalDateTime.now();
    }
}
