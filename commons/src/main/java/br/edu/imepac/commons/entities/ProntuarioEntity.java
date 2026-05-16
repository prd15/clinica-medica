package br.edu.imepac.commons.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Prontuario gerado a partir de um atendimento.
 * Tabela: prontuarios (banco clinica_atendimento)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prontuarios")
public class ProntuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long atendimentoId;

    @Column(nullable = false)
    private Long pacienteId;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(length = 2000)
    private String historico;
}
