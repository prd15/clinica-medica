package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados retornados de uma especialidade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadeResponse {

    @Schema(description = "ID da especialidade", example = "1")
    private Long id;

    @Schema(description = "Nome da especialidade", example = "Cardiologia")
    private String nome;

    @Schema(description = "Descrição da especialidade", example = "Especialidade do coração e sistema cardiovascular")
    private String descricao;
}
