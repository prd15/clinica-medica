package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// response de medico — sem campo senha, independente do que estiver na entity
@Schema(description = "Dados retornados de um médico (senha omitida por segurança)")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoResponse {

    @Schema(description = "ID do médico", example = "1")
    private Long id;

    @Schema(description = "Nome completo", example = "Dr. Carlos Silva")
    private String nome;

    @Schema(description = "CRM do médico", example = "CRM-MG-12345")
    private String crm;

    @Schema(description = "Telefone de contato", example = "34988887777")
    private String telefone;

    @Schema(description = "Indica se o médico está ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Especialidades associadas ao médico")
    private Set<EspecialidadeResponse> especialidades;
}
