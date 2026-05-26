package br.edu.imepac.atendimento.anotacao;

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
@Table(name = "anotacoes")
public class AnotacaoEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long prontuarioId;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private LocalDateTime dataCriacao;
}
