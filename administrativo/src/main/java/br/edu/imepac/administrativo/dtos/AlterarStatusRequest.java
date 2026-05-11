package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO minimo pro PATCH de status — so precisa do campo ativo
@Schema(description = "Payload para alterar status do convênio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlterarStatusRequest {

    @Schema(description = "true para ativar, false para inativar", example = "false")
    @NotNull(message = "Campo ativo é obrigatório")
    private Boolean ativo;
}
