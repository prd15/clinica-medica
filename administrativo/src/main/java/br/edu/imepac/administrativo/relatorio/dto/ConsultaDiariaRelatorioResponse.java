package br.edu.imepac.administrativo.relatorio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Relatorio de consultas agrupadas por data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaDiariaRelatorioResponse {

    @Schema(description = "Data consultada", example = "2026-05-12")
    private LocalDate data;

    @Schema(description = "Total de consultas na data informada", example = "8")
    private Integer totalConsultas;
}
