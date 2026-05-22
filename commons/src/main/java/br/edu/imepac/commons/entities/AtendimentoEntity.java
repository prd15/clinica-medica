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

    // 1:1 com a consulta — cada consulta gera no maximo um atendimento
    @Column(nullable = false, unique = true)
    private Long consultaId;

    @Column(nullable = false)
    private Long medicoId;

    @Column(nullable = false)
    private Long pacienteId;

    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private StatusAtendimento status;
}
