package br.edu.imepac.commons.entities.atendimento;

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
@Table(name = "solicitacoes_exame")
public class SolicitacaoExameEntity extends BaseEntity {

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
