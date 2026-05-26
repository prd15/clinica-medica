package br.edu.imepac.agendamento.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Resposta do endpoint de contagem — usado por relatorios para evitar baixar
// a lista inteira de consultas so para contar.
@Schema(description = "Total de consultas em uma data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContagemConsultasResponse {

    @Schema(description = "Data consultada", example = "2026-05-23")
    private LocalDate data;

    @Schema(description = "Total de consultas no dia", example = "42")
    private long total;
}
