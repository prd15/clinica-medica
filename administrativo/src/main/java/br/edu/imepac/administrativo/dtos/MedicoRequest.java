package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// request de medico — senha e obrigatoria aqui mas nunca aparece no response
@Schema(description = "Dados para criação ou atualização de médico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoRequest {

    @NotBlank(message = "O nome do médico é obrigatório")
    @Schema(description = "Nome completo", example = "Dr. Carlos Silva")
    private String nome;

    @NotBlank(message = "O CRM é obrigatório")
    @Schema(description = "CRM do médico (usado como login)", example = "CRM-MG-12345")
    private String crm;

    @NotBlank(message = "A senha é obrigatória")
    @Schema(description = "Senha de acesso", example = "senha123", accessMode = AccessMode.WRITE_ONLY)
    private String senha;

    @Schema(description = "Telefone de contato", example = "34988887777")
    private String telefone;
}
