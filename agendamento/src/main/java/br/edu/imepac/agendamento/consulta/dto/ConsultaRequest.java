package br.edu.imepac.agendamento.consulta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados para agendamento de consulta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaRequest {

    @Schema(description = "ID do paciente", example = "1")
    @NotNull(message = "O paciente é obrigatório")
    private Long pacienteId;

    @Schema(description = "ID do médico", example = "1")
    @NotNull(message = "O médico é obrigatório")
    private Long medicoId;

    @Schema(description = "ID do convênio", example = "1")
    @NotNull(message = "O convênio é obrigatório")
    private Long convenioId;

    @Schema(description = "Data e hora da consulta", example = "2026-08-01T10:00:00")
    @NotNull(message = "A data e hora são obrigatórias")
    @Future(message = "A data da consulta nao pode estar no passado")
    private LocalDateTime dataHora;

    @Schema(description = "Observações adicionais", example = "Consulta de rotina")
    @Size(max = 500, message = "Observacoes nao podem ter mais de 500 caracteres")
    private String observacoes;
}
