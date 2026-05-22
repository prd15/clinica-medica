package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados retornados de um atendente, com senha omitida por seguranca")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendenteResponse {

    @Schema(description = "ID do atendente", example = "1")
    private Long id;

    @Schema(description = "Nome completo do atendente", example = "Maria Recepcao")
    private String nome;

    @Schema(description = "Usuario unico de acesso", example = "maria.recepcao")
    private String usuario;

    @Schema(description = "Indica se o atendente esta ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Data e hora de criacao do registro", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da ultima atualizacao do registro", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;
}
