package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.ConsultaDiariaRelatorioResponse;
import br.edu.imepac.administrativo.dtos.PacienteResponse;
import br.edu.imepac.administrativo.dtos.PacientesPorConvenioRelatorioResponse;
import br.edu.imepac.commons.services.PacienteService;
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
    private final ModelMapper modelMapper;

    public RelatorioController(PacienteService pacienteService, ModelMapper modelMapper) {
        this.pacienteService = pacienteService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Relatorio de consultas diarias")
    @ApiResponse(responseCode = "200", description = "Relatorio retornado com sucesso")
    @GetMapping("/consultas-diarias")
    public ResponseEntity<ConsultaDiariaRelatorioResponse> consultasDiarias(
            @RequestParam
            @Parameter(description = "Data do relatorio no formato yyyy-MM-dd", example = "2026-05-12")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data) {
        return ResponseEntity.ok(new ConsultaDiariaRelatorioResponse(data, 0));
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
