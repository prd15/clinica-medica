package br.edu.imepac.administrativo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadeRequest {

    @NotBlank(message = "O nome da especialidade é obrigatório")
    private String nome;

    private String descricao;
}
