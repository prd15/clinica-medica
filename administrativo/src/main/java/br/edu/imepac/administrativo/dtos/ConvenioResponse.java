package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados retornados de um convênio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioResponse {

    @Schema(description = "ID do convênio", example = "1")
    private Long id;

    @Schema(description = "Nome do convênio", example = "Unimed")
    private String nome;

    @Schema(description = "Descrição do convênio", example = "Plano regional de saúde")
    private String descricao;

    @Schema(description = "CNPJ do convênio", example = "12.345.678/0001-99")
    private String cnpj;

    @Schema(description = "Telefone de contato do convênio", example = "(34) 99999-9999")
    private String telefone;

    @Schema(description = "Indica se o convênio está ativo", example = "true")
    private Boolean ativo;
}

