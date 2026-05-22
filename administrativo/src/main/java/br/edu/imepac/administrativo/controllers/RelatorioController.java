package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.clients.AgendamentoClient;
import br.edu.imepac.administrativo.dtos.ConsultaDiariaRelatorioResponse;
import br.edu.imepac.administrativo.dtos.PacienteResponse;
import br.edu.imepac.administrativo.dtos.PacientesPorConvenioRelatorioResponse;
import br.edu.imepac.commons.services.administrativo.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Relatorios", description = "Relatorios gerenciais do administrativo")
@RestController
@RequestMapping("/v1/relatorios")
public class RelatorioController {

    private final PacienteService pacienteService;
    private final AgendamentoClient agendamentoClient;
    private final ModelMapper modelMapper;

    public RelatorioController(PacienteService pacienteService,
                               AgendamentoClient agendamentoClient,
                               ModelMapper modelMapper) {
        this.pacienteService = pacienteService;
        this.agendamentoClient = agendamentoClient;
        this.modelMapper = modelMapper;
    }

    // consulta o microsservico de agendamento (GET /v1/consultas?data=X) e conta o total.
    // se o agendamento estiver fora, AgendamentoClient devolve lista vazia (fail-safe).
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
        int total = agendamentoClient.listarConsultasPorData(data).size();
        return ResponseEntity.ok(new ConsultaDiariaRelatorioResponse(data, total));
    }

    @Operation(summary = "Relatorio de pacientes por convenio")
    @ApiResponse(responseCode = "200", description = "Relatorio retornado com sucesso")
    @GetMapping("/pacientes-por-convenio")
    public ResponseEntity<PacientesPorConvenioRelatorioResponse> pacientesPorConvenio(
            @Parameter(description = "ID do convenio", example = "1")
            @RequestParam Long convenioId) {
        List<PacienteResponse> pacientes = pacienteService.findByConvenioId(convenioId)
                .stream()
                .map(entity -> modelMapper.map(entity, PacienteResponse.class))
                .toList();
        return ResponseEntity.ok(new PacientesPorConvenioRelatorioResponse(
                convenioId,
                pacientes.size(),
                pacientes
        ));
    }
}
