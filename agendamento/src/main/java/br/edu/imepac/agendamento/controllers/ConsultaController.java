package br.edu.imepac.agendamento.controllers;

import br.edu.imepac.agendamento.clients.AdministrativoClient;
import br.edu.imepac.agendamento.dtos.ConsultaRequest;
import br.edu.imepac.agendamento.dtos.ConsultaResponse;
import br.edu.imepac.agendamento.dtos.ReagendarRequest;
import br.edu.imepac.commons.entities.ConsultaEntity;
import br.edu.imepac.commons.services.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Consultas", description = "Agendamento e gestao de consultas medicas")
@RestController
@RequestMapping("/v1/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final AdministrativoClient administrativoClient;
    private final ModelMapper modelMapper;

    public ConsultaController(ConsultaService consultaService,
                              AdministrativoClient administrativoClient,
                              ModelMapper modelMapper) {
        this.consultaService = consultaService;
        this.administrativoClient = administrativoClient;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Agenda uma nova consulta",
            description = "Valida conflito de horario e status do convenio antes de agendar")
    @ApiResponse(responseCode = "201", description = "Consulta agendada com sucesso")
    @ApiResponse(responseCode = "400", description = "Conflito de horario ou convenio inativo")
    @PostMapping
    public ResponseEntity<?> agendar(@Valid @RequestBody ConsultaRequest request) {
        // validacao de convenio ativo acontece ANTES do service — depende de HTTP no administrativo
        if (!administrativoClient.isConvenioAtivo(request.getConvenioId())) {
            return ResponseEntity.badRequest().body("Convenio inativo ou nao encontrado");
        }
        try {
            ConsultaEntity entity = modelMapper.map(request, ConsultaEntity.class);
            ConsultaEntity salva = consultaService.agendar(entity);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(modelMapper.map(salva, ConsultaResponse.class));
        } catch (RuntimeException e) {
            // service joga RuntimeException quando ha conflito de horario
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Cancela uma consulta agendada")
    @ApiResponse(responseCode = "200", description = "Consulta cancelada")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<ConsultaResponse> cancelar(@PathVariable("id") Long id) {
        return consultaService.cancelar(id)
                .map(c -> ResponseEntity.ok(modelMapper.map(c, ConsultaResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }
}
