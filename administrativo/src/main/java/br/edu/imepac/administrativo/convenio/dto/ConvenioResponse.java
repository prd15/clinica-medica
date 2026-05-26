package br.edu.imepac.administrativo.convenio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados retornados de um convenio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioResponse {

    @Schema(description = "ID do convenio", example = "1")
    private Long id;

    @Schema(description = "Nome do convenio", example = "Unimed")
    private String nome;

    @Schema(description = "Descricao do convenio", example = "Plano regional de saude")
    private String descricao;

    @Schema(description = "CNPJ do convenio", example = "12.345.678/0001-99")
    private String cnpj;

    @Schema(description = "Telefone de contato do convenio", example = "(34) 99999-9999")
    private String telefone;

    @Schema(description = "Indica se o convenio esta ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Data e hora de criacao do registro", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da ultima atualizacao do registro", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;
}
