package br.edu.imepac.administrativo.relatorio;

import br.edu.imepac.administrativo.relatorio.dto.ConsultaDiariaRelatorioResponse;
import br.edu.imepac.administrativo.relatorio.dto.PacientesPorConvenioRelatorioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Relatorios", description = "Relatorios gerenciais do administrativo")
@RestController
@RequestMapping("/v1/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @Operation(summary = "Relatorio de consultas diarias",
            description = "Conta consultas agendadas para a data informada via integracao com o agendamento")
    @ApiResponse(responseCode = "200", description = "Relatorio retornado com sucesso")
    @ApiResponse(responseCode = "400", description = "Data invalida ou ausente")
    @GetMapping("/consultas-diarias")
    public ResponseEntity<ConsultaDiariaRelatorioResponse> consultasDiarias(
            @RequestParam
            @Parameter(description = "Data do relatorio no formato yyyy-MM-dd", example = "2026-05-12")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data) {
        return ResponseEntity.ok(relatorioService.consultasDiarias(data));
    }

    @Operation(summary = "Relatorio de pacientes por convenio")
    @ApiResponse(responseCode = "200", description = "Relatorio retornado com sucesso")
    @ApiResponse(responseCode = "400", description = "ID do convenio ausente ou em formato invalido")
    @GetMapping("/pacientes-por-convenio")
    public ResponseEntity<PacientesPorConvenioRelatorioResponse> pacientesPorConvenio(
            @Parameter(description = "ID do convenio", example = "1")
            @RequestParam Long convenioId) {
        return ResponseEntity.ok(relatorioService.pacientesPorConvenio(convenioId));
    }
}
