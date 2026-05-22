package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 200, message = "Nome nao pode ter mais de 200 caracteres")
    @Schema(description = "Nome completo", example = "Dr. Carlos Silva")
    private String nome;

    @NotBlank(message = "O CRM é obrigatório")
    @Size(max = 20, message = "CRM nao pode ter mais de 20 caracteres")
    @Schema(description = "CRM do médico (usado como login)", example = "CRM-MG-12345")
    private String crm;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    @Schema(description = "Senha de acesso", example = "senha123", accessMode = AccessMode.WRITE_ONLY)
    private String senha;

    @Schema(description = "Telefone de contato", example = "34988887777")
    @Size(max = 20, message = "Telefone nao pode ter mais de 20 caracteres")
    private String telefone;
}
