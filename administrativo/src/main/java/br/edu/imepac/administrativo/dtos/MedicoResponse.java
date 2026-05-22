package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Dados retornados de um medico, com senha omitida por seguranca")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoResponse {

    @Schema(description = "ID do medico", example = "1")
    private Long id;

    @Schema(description = "Nome completo", example = "Dr. Carlos Silva")
    private String nome;

    @Schema(description = "CRM do medico", example = "CRM-MG-12345")
    private String crm;

    @Schema(description = "Telefone de contato", example = "34988887777")
    private String telefone;

    @Schema(description = "Indica se o medico esta ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Especialidades associadas ao medico")
    private Set<EspecialidadeResponse> especialidades;

    @Schema(description = "Data e hora de criacao do registro", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da ultima atualizacao do registro", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;
}
