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

import java.time.LocalDate;
import java.util.List;

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

    @Operation(summary = "Reagenda uma consulta para outra data e hora")
    @ApiResponse(responseCode = "200", description = "Consulta reagendada")
    @ApiResponse(responseCode = "400", description = "Conflito de horario no novo horario")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada")
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendar(@PathVariable("id") Long id,
                                       @Valid @RequestBody ReagendarRequest request) {
        try {
            return consultaService.reagendar(id, request.getDataHora())
                    .map(c -> ResponseEntity.ok((Object) modelMapper.map(c, ConsultaResponse.class)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Confirma uma consulta pendente")
    @ApiResponse(responseCode = "200", description = "Consulta confirmada")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada")
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ConsultaResponse> confirmar(@PathVariable("id") Long id) {
        return consultaService.confirmar(id)
                .map(c -> ResponseEntity.ok(modelMapper.map(c, ConsultaResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista consultas com filtros opcionais",
            description = "Prioridade dos filtros: medicoId > pacienteId > data > sem filtro")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listar(
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) String data) {

        List<ConsultaEntity> consultas;
        if (medicoId != null) {
            consultas = consultaService.findByMedicoId(medicoId);
        } else if (pacienteId != null) {
            consultas = consultaService.findByPacienteId(pacienteId);
        } else if (data != null && !data.isBlank()) {
            // formato esperado yyyy-MM-dd — LocalDate.parse ja valida
            consultas = consultaService.findByData(LocalDate.parse(data));
        } else {
            // sem filtro: retorna agenda de quem? — nenhuma; devolve vazio.
            // (evita escanear tudo no banco; chamada deve sempre vir com filtro)
            consultas = List.of();
        }

        List<ConsultaResponse> response = consultas.stream()
                .map(c -> modelMapper.map(c, ConsultaResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retorna agenda do medico com consultas pendentes")
    @ApiResponse(responseCode = "200", description = "Agenda retornada")
    @GetMapping("/minha-agenda")
    public ResponseEntity<List<ConsultaResponse>> minhaAgenda(@RequestParam Long medicoId) {
        List<ConsultaResponse> response = consultaService.findMinhaAgenda(medicoId).stream()
                .map(c -> modelMapper.map(c, ConsultaResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }
}
