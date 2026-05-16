package br.edu.imepac.commons.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Atendimento realizado em consulta.
 * Tabela: atendimentos (banco clinica_atendimento)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "atendimentos")
public class AtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long consultaId;

    @Column(nullable = false)
    private Long medicoId;

    @Column(nullable = false)
    private Long pacienteId;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 1000)
    private String descricao;

    @Column(length = 500)
    private String diagnostico;

    @Column(length = 1000)
    private String observacoes;
}
