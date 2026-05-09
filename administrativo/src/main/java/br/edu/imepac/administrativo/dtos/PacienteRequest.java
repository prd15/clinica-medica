package br.edu.imepac.administrativo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// dados que chegam do front pra criar/atualizar paciente
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteRequest {

    @NotBlank(message = "O nome do paciente e obrigatorio")
    private String nome;

    @NotBlank(message = "O CPF do paciente e obrigatorio")
    private String cpf;

    private String telefone;
    private String email;
    private String endereco;
    private Long convenioId;
}
