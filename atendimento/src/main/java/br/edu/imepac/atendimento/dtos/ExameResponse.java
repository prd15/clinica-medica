package br.edu.imepac.atendimento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados de uma solicitacao de exame")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExameResponse {

    @Schema(description = "ID da solicitacao", example = "1")
    private Long id;

    @Schema(description = "ID do atendimento relacionado", example = "1")
    private Long atendimentoId;

    @Schema(description = "Descricao do exame", example = "Hemograma completo")
    private String descricao;

    @Schema(description = "Tipo do exame", example = "LABORATORIAL")
    private String tipo;

    @Schema(description = "Data e hora da solicitacao", example = "2026-05-15T10:30:00")
    private LocalDateTime dataSolicitacao;
}
