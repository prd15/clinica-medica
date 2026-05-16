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
@Table(name = "prontuarios")
public class ProntuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long atendimentoId;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    private LocalDateTime dataCriacao;
}
