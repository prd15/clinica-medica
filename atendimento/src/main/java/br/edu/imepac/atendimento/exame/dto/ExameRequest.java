package br.edu.imepac.atendimento.exame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados para solicitar um exame")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExameRequest {

    @NotBlank(message = "descricao não pode ser vazia")
    @Size(max = 500, message = "Descricao nao pode ter mais de 500 caracteres")
    @Schema(description = "Descrição do exame solicitado", example = "Hemograma completo")
    private String descricao;

    @Pattern(regexp = "LABORATORIAL|IMAGEM|FUNCIONAL",
            message = "Tipo deve ser LABORATORIAL, IMAGEM ou FUNCIONAL")
    @Schema(description = "Tipo do exame", example = "LABORATORIAL",
            allowableValues = {"LABORATORIAL", "IMAGEM", "FUNCIONAL"})
    private String tipo;
}
