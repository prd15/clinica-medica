package br.edu.imepac.atendimento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados de uma anotacao do prontuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnotacaoResponse {

    @Schema(description = "ID da anotacao", example = "1")
    private Long id;

    @Schema(description = "ID do prontuario", example = "1")
    private Long prontuarioId;

    @Schema(description = "Texto da anotacao")
    private String texto;

    @Schema(description = "Data e hora de criacao", example = "2026-05-15T10:30:00")
    private LocalDateTime dataCriacao;
}
