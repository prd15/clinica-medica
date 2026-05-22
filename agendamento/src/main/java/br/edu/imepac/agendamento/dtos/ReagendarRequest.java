package br.edu.imepac.agendamento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados para reagendamento de consulta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReagendarRequest {

    @Schema(description = "Nova data e hora", example = "2026-08-10T14:00:00")
    @NotNull(message = "A nova data e hora são obrigatórias")
    @Future(message = "A nova data nao pode estar no passado")
    private LocalDateTime dataHora;
}
