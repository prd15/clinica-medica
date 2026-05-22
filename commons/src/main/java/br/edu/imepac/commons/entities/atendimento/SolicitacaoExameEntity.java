package br.edu.imepac.commons.entities.atendimento;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitacoes_exame")
public class SolicitacaoExameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long atendimentoId;

    private String descricao;

    // ex: "LABORATORIAL", "IMAGEM", "FUNCIONAL"
    private String tipo;

    private LocalDateTime dataSolicitacao;
}
