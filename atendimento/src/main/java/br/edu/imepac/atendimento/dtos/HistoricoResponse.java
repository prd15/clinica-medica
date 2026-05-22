package br.edu.imepac.atendimento.dtos;

import br.edu.imepac.commons.entities.atendimento.StatusAtendimento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Item do historico de atendimentos do paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoResponse {

    @Schema(description = "ID do atendimento", example = "1")
    private Long id;

    @Schema(description = "ID da consulta relacionada", example = "1")
    private Long consultaId;

    @Schema(description = "ID do medico", example = "3")
    private Long medicoId;

    @Schema(description = "ID do paciente", example = "7")
    private Long pacienteId;

    @Schema(description = "Data e hora do atendimento", example = "2026-05-15T10:30:00")
    private LocalDateTime dataHora;

    @Schema(description = "Status do atendimento", example = "REALIZADO")
    private StatusAtendimento status;

    @Schema(description = "Data e hora de criacao do registro", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da ultima atualizacao do registro", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;
}
