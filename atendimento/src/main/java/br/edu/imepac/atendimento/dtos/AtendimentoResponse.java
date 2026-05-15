package br.edu.imepac.atendimento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados de retorno de um atendimento registrado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoResponse {

    @Schema(description = "ID do atendimento", example = "1")
    private Long id;

    @Schema(description = "ID da consulta relacionada", example = "1")
    private Long consultaId;

    @Schema(description = "ID do médico", example = "3")
    private Long medicoId;

    @Schema(description = "ID do paciente", example = "7")
    private Long pacienteId;

    @Schema(description = "Data e hora do atendimento", example = "2025-06-10T14:30:00")
    private LocalDateTime dataHora;

    @Schema(description = "Status do atendimento", example = "REALIZADO")
    private String status;

    @Schema(description = "ID do prontuário gerado")
    private Long prontuarioId;
}
