package br.edu.imepac.commons.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "atendimentos")
public class AtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // id da consulta — referencia ao servico de agendamento, sem FK entre bancos
    @Column(nullable = false)
    private Long consultaId;

    // ids como Long — bancos separados, sem @ManyToOne entre servicos
    @Column(nullable = false)
    private Long medicoId;

    @Column(nullable = false)
    private Long pacienteId;

    private LocalDateTime dataHora;

    // ex: "REALIZADO", "EM_ANDAMENTO"
    private String status;
}
