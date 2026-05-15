package br.edu.imepac.atendimento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados para adicionar uma anotação ao prontuário")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnotacaoRequest {

    @NotBlank(message = "texto não pode ser vazio")
    @Schema(description = "Texto da anotação clínica", example = "Paciente voltou para revisão. Melhora significativa.")
    private String texto;
}
