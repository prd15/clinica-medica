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
@Table(name = "consultas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_consulta_medico_data_hora",
                columnNames = {"medico_id", "data_hora"}
        ),
        indexes = {
                @Index(name = "idx_consulta_medico_data_hora", columnList = "medico_id, data_hora"),
                @Index(name = "idx_consulta_paciente", columnList = "paciente_id")
        })
public class ConsultaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // referencia por id — paciente vive no banco do administrativo, sem FK entre bancos
    @Column(nullable = false)
    private Long pacienteId;

    // referencia por id — medico vive no banco do administrativo, sem FK entre bancos
    @Column(nullable = false)
    private Long medicoId;

    // referencia por id — convenio vive no banco do administrativo, sem FK entre bancos
    @Column(nullable = false)
    private Long convenioId;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    // toda consulta nasce PENDENTE; muda via confirmar/cancelar/reagendar
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status = StatusConsulta.PENDENTE;

    @Column(length = 500)
    private String observacoes;
}
