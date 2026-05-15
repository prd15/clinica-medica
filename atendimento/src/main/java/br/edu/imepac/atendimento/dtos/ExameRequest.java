package br.edu.imepac.atendimento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados para solicitar um exame")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExameRequest {

    @NotBlank(message = "descricao não pode ser vazia")
    @Schema(description = "Descrição do exame solicitado", example = "Hemograma completo")
    private String descricao;

    @Schema(description = "Tipo do exame", example = "LABORATORIAL",
            allowableValues = {"LABORATORIAL", "IMAGEM", "FUNCIONAL"})
    private String tipo;
}
