package br.edu.imepac.agendamento.consulta;

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
// nao usamos unique (medico_id, data_hora) porque uma consulta CANCELADA libera o slot
// para nova marcacao no mesmo horario; a checagem fica por conta do ConsultaService.existeConflito
// dentro do @Transactional do metodo agendar/reagendar (excluindo status CANCELADA)
@Table(name = "consultas",
        indexes = {
                @Index(name = "idx_consulta_medico_data_hora", columnList = "medico_id, data_hora"),
                @Index(name = "idx_consulta_paciente", columnList = "paciente_id")
        })
public class ConsultaEntity extends BaseEntity {

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
