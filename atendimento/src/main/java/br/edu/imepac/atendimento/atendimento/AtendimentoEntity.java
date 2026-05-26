package br.edu.imepac.atendimento.atendimento;

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
@Table(name = "atendimentos")
public class AtendimentoEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:1 com a consulta — cada consulta gera no maximo um atendimento
    @Column(nullable = false, unique = true)
    private Long consultaId;

    @Column(nullable = false)
    private Long medicoId;

    @Column(nullable = false)
    private Long pacienteId;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAtendimento status;
}
