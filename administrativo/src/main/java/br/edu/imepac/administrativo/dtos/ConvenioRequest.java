package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados para criação ou atualização de convênio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioRequest {

    @Schema(description = "Nome do convênio", example = "Unimed")
    @NotBlank(message = "O nome do convênio é obrigatório")
    private String nome;

    @Schema(description = "Descrição do convênio", example = "Plano regional de saúde")
    private String descricao;

    @Schema(description = "CNPJ do convênio", example = "12.345.678/0001-99")
    @NotBlank(message = "CNPJ é obrigatório")
    private String cnpj;

    @Schema(description = "Telefone de contato do convênio", example = "(34) 99999-9999")
    private String telefone;

    @Schema(description = "Indica se o convênio está ativo", example = "true")
    private Boolean ativo = true;
}

