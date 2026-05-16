package br.edu.imepac.agendamento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Payload padrao de erro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @Schema(description = "Momento em que o erro foi gerado", example = "2026-05-15T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "Codigo HTTP do erro", example = "400")
    private int status;

    @Schema(description = "Descricao curta do erro", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem detalhada do erro")
    private Object message;
}
