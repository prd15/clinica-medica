package br.edu.imepac.atendimento.prontuario;

import br.edu.imepac.commons.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prontuarios")
public class ProntuarioEntity extends BaseEntity {

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
