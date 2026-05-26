package br.edu.imepac.atendimento.atendimento.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados para registrar um atendimento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoRequest {

    @NotNull(message = "consultaId é obrigatório")
    @Schema(description = "ID da consulta agendada", example = "1")
    private Long consultaId;

    @NotNull(message = "medicoId é obrigatório")
    @Schema(description = "ID do médico responsável", example = "3")
    private Long medicoId;

    @NotNull(message = "pacienteId é obrigatório")
    @Schema(description = "ID do paciente atendido", example = "7")
    private Long pacienteId;

    @Schema(description = "Descrição clínica do atendimento", example = "Paciente relatou dor abdominal há 3 dias")
    @Size(max = 2000, message = "Descricao nao pode ter mais de 2000 caracteres")
    private String descricao;

    @Schema(description = "Diagnóstico do médico", example = "Gastrite aguda")
    @Size(max = 1000, message = "Diagnostico nao pode ter mais de 1000 caracteres")
    private String diagnostico;

    @Schema(description = "Observações adicionais", example = "Recomendado repouso e dieta leve")
    @Size(max = 2000, message = "Observacoes nao podem ter mais de 2000 caracteres")
    private String observacoes;
}
