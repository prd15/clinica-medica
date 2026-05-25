package br.edu.imepac.agendamento.dtos;

import br.edu.imepac.commons.entities.agendamento.StatusConsulta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados retornados de uma consulta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaResponse {

    @Schema(description = "Identificador da consulta", example = "1")
    private Long id;

    @Schema(description = "ID do paciente", example = "1")
    private Long pacienteId;

    @Schema(description = "ID do medico", example = "1")
    private Long medicoId;

    @Schema(description = "ID do convenio", example = "1")
    private Long convenioId;

    @Schema(description = "Data e hora da consulta", example = "2026-08-01T10:00:00")
    private LocalDateTime dataHora;

    @Schema(description = "Status atual da consulta",
            example = "PENDENTE",
            allowableValues = {"PENDENTE", "CONFIRMADA", "REALIZADA", "CANCELADA"})
    private StatusConsulta status;

    @Schema(description = "Observacoes adicionais", example = "Consulta de rotina")
    private String observacoes;

    @Schema(description = "Data e hora de criacao do registro", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da ultima atualizacao do registro", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;
}
