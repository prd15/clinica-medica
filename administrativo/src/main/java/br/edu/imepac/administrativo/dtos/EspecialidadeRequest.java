package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados para criação ou atualização de especialidade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadeRequest {

    @NotBlank(message = "O nome da especialidade é obrigatório")
    @Schema(description = "Nome da especialidade", example = "Cardiologia")
    private String nome;

    @Schema(description = "Descrição", example = "Especialidade do coração e sistema cardiovascular")
    private String descricao;
}
