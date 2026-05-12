package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.ConsultaDiariaRelatorioResponse;
import br.edu.imepac.administrativo.dtos.PacienteResponse;
import br.edu.imepac.administrativo.dtos.PacientesPorConvenioRelatorioResponse;
import br.edu.imepac.commons.services.PacienteService;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/relatorios")
public class RelatorioController {

    private final PacienteService pacienteService;
    private final ModelMapper modelMapper;

    public RelatorioController(PacienteService pacienteService, ModelMapper modelMapper) {
        this.pacienteService = pacienteService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/consultas-diarias")
    public ResponseEntity<ConsultaDiariaRelatorioResponse> consultasDiarias(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data) {
        return ResponseEntity.ok(new ConsultaDiariaRelatorioResponse(data, 0));
    }

    @GetMapping("/pacientes-por-convenio")
    public ResponseEntity<PacientesPorConvenioRelatorioResponse> pacientesPorConvenio(
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
