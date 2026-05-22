package br.edu.imepac.atendimento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados retornados de um prontuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProntuarioResponse {

    @Schema(description = "ID do prontuario", example = "1")
    private Long id;

    @Schema(description = "ID do atendimento relacionado", example = "1")
    private Long atendimentoId;

    @Schema(description = "Descricao clinica do atendimento", example = "Paciente relatou dor abdominal")
    private String descricao;

    @Schema(description = "Diagnostico do medico", example = "Gastrite aguda")
    private String diagnostico;

    @Schema(description = "Observacoes adicionais", example = "Recomendado repouso")
    private String observacoes;

    @Schema(description = "Data e hora de criacao clinica do prontuario", example = "2026-05-15T10:30:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data e hora de criacao do registro", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da ultima atualizacao do registro", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;
}
