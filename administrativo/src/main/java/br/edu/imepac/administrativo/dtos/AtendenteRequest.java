package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados para criacao ou atualizacao de atendente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendenteRequest {

    @NotBlank(message = "O nome do atendente e obrigatorio")
    @Schema(description = "Nome completo do atendente", example = "Maria Recepcao")
    private String nome;

    @NotBlank(message = "O usuario do atendente e obrigatorio")
    @Schema(description = "Usuario unico de acesso", example = "maria.recepcao")
    private String usuario;

    @NotBlank(message = "A senha do atendente e obrigatoria")
    @Schema(description = "Senha de acesso", example = "senha123", accessMode = AccessMode.WRITE_ONLY)
    private String senha;
}
