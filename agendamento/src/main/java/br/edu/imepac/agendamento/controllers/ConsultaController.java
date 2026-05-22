package br.edu.imepac.agendamento.controllers;

import br.edu.imepac.agendamento.clients.AdministrativoClient;
import br.edu.imepac.agendamento.dtos.ConsultaRequest;
import br.edu.imepac.agendamento.dtos.ConsultaResponse;
import br.edu.imepac.agendamento.dtos.ReagendarRequest;
import br.edu.imepac.commons.entities.agendamento.ConsultaEntity;
import br.edu.imepac.commons.services.agendamento.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
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
    @ApiResponse(responseCode = "400", description = "Convenio inativo ou dados invalidos")
    @ApiResponse(responseCode = "409", description = "Conflito de horario")
    @PostMapping
    public ResponseEntity<ConsultaResponse> agendar(@Valid @RequestBody ConsultaRequest request) {
        if (!administrativoClient.isConvenioAtivo(request.getConvenioId())) {
            throw new IllegalArgumentException("Convenio inativo ou nao encontrado");
        }
        if (!administrativoClient.isMedicoAtivo(request.getMedicoId())) {
            throw new IllegalArgumentException("Medico inativo ou nao encontrado");
        }
        if (!administrativoClient.isPacienteExistente(request.getPacienteId())) {
            throw new IllegalArgumentException("Paciente nao encontrado");
        }
        ConsultaEntity entity = new ConsultaEntity();
        entity.setPacienteId(request.getPacienteId());
        entity.setMedicoId(request.getMedicoId());
        entity.setConvenioId(request.getConvenioId());
        entity.setDataHora(request.getDataHora());
        entity.setObservacoes(request.getObservacoes());

        ConsultaEntity salva = consultaService.agendar(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(salva, ConsultaResponse.class));
    }

    @Operation(summary = "Busca uma consulta por ID")
    @ApiResponse(responseCode = "200", description = "Consulta encontrada")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> buscarPorId(@PathVariable("id") Long id) {
        return consultaService.findById(id)
                .map(c -> ResponseEntity.ok(modelMapper.map(c, ConsultaResponse.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cancela uma consulta agendada")
    @ApiResponse(responseCode = "200", description = "Consulta cancelada")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada")
    @ApiResponse(responseCode = "409", description = "Consulta nao pode ser cancelada no status atual")
    @DeleteMapping("/{id}")
    public ResponseEntity<ConsultaResponse> cancelar(@PathVariable("id") Long id) {
        return consultaService.cancelar(id)
                .map(c -> ResponseEntity.ok(modelMapper.map(c, ConsultaResponse.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Reagenda uma consulta para outra data e hora")
    @ApiResponse(responseCode = "200", description = "Consulta reagendada")
    @ApiResponse(responseCode = "400", description = "Data no passado")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada")
    @ApiResponse(responseCode = "409", description = "Conflito de horario ou status terminal")
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<ConsultaResponse> reagendar(@PathVariable("id") Long id,
                                                     @Valid @RequestBody ReagendarRequest request) {
        return consultaService.reagendar(id, request.getDataHora())
                .map(c -> ResponseEntity.ok(modelMapper.map(c, ConsultaResponse.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Confirma uma consulta pendente")
    @ApiResponse(responseCode = "200", description = "Consulta confirmada")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada")
    @ApiResponse(responseCode = "409", description = "Consulta nao esta no status PENDENTE")
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ConsultaResponse> confirmar(@PathVariable("id") Long id) {
        return consultaService.confirmar(id)
                .map(c -> ResponseEntity.ok(modelMapper.map(c, ConsultaResponse.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // chamado pelo microsservico de atendimento quando o atendimento e registrado
    @Operation(summary = "Marca uma consulta como realizada")
    @ApiResponse(responseCode = "200", description = "Consulta marcada como realizada")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada")
    @ApiResponse(responseCode = "409", description = "Consulta cancelada ou ja realizada")
    @PatchMapping("/{id}/realizar")
    public ResponseEntity<ConsultaResponse> realizar(@PathVariable("id") Long id) {
        return consultaService.realizar(id)
                .map(c -> ResponseEntity.ok(modelMapper.map(c, ConsultaResponse.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista consultas com filtros opcionais",
            description = "Combina medicoId + data (agenda do medico no dia). Sozinhos: medicoId, pacienteId ou data. Informe ao menos um filtro.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "400", description = "Nenhum filtro informado ou data invalida")
    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listar(
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        List<ConsultaEntity> consultas;
        if (medicoId != null && data != null) {
            consultas = consultaService.findByMedicoIdAndData(medicoId, data);
        } else if (medicoId != null) {
            consultas = consultaService.findByMedicoId(medicoId);
        } else if (pacienteId != null) {
            consultas = consultaService.findByPacienteId(pacienteId);
        } else if (data != null) {
            consultas = consultaService.findByData(data);
        } else {
            throw new IllegalArgumentException(
                    "Informe ao menos um filtro: medicoId, pacienteId ou data");
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
