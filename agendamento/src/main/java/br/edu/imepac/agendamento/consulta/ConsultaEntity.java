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
// Guarda de double-booking no nivel do banco, sem quebrar a regra "CANCELADA libera o slot":
// slotAtivo = dataHora enquanto a consulta esta ativa, NULL quando CANCELADA. O unique
// (medico_id, slot_ativo) impede duas consultas ATIVAS no mesmo horario para o mesmo medico
// (multiplos NULL nao colidem no MySQL), inclusive sob concorrencia — fecha a janela de race
// que o existeConflito do service sozinho nao cobre. A checagem por janela (+-30min) continua
// no service para UX; o unique e a ultima linha contra slot exato concorrente.
@Table(name = "consultas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_consulta_medico_slot_ativo", columnNames = {"medico_id", "slot_ativo"})
        },
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

    // espelho de dataHora usado so para o unique de slot; NULL quando CANCELADA (libera o slot).
    // Mantido pelo ConsultaService nas transicoes (agendar/reagendar/cancelar).
    @Column(name = "slot_ativo")
    private LocalDateTime slotAtivo;

    // toda consulta nasce PENDENTE; muda via confirmar/cancelar/reagendar
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status = StatusConsulta.PENDENTE;

    @Column(length = 500)
    private String observacoes;
}
