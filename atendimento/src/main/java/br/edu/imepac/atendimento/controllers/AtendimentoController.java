package br.edu.imepac.atendimento.controllers;

import br.edu.imepac.atendimento.clients.AgendamentoClient;
import br.edu.imepac.atendimento.dtos.AtendimentoRequest;
import br.edu.imepac.atendimento.dtos.AtendimentoResponse;
import br.edu.imepac.commons.entities.AtendimentoEntity;
import br.edu.imepac.commons.services.AtendimentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/atendimentos")
@Tag(name = "Atendimentos", description = "Registro clínico de consultas realizadas")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;
    private final AgendamentoClient agendamentoClient;
    private final ModelMapper modelMapper;

    public AtendimentoController(AtendimentoService atendimentoService,
                                 AgendamentoClient agendamentoClient,
                                 ModelMapper modelMapper) {
        this.atendimentoService = atendimentoService;
        this.agendamentoClient = agendamentoClient;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<AtendimentoResponse> realizar(@Valid @RequestBody AtendimentoRequest request) {
        AtendimentoEntity entidade = modelMapper.map(request, AtendimentoEntity.class);

        AtendimentoEntity salvo = atendimentoService.registrar(
                entidade,
                request.getDescricao(),
                request.getDiagnostico(),
                request.getObservacoes()
        );

        // busca prontuarioId antes de notificar o agendamento para incluir no response
        Long prontuarioId = atendimentoService.buscarProntuario(salvo.getId()).getId();

        try {
            agendamentoClient.confirmarRealizacao(request.getConsultaId());
        } catch (Exception e) {
            log.warn("Nao foi possivel notificar o agendamento para consultaId {}: {}", request.getConsultaId(), e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo, prontuarioId));
    }

    private AtendimentoResponse toResponse(AtendimentoEntity entity, Long prontuarioId) {
        AtendimentoResponse response = modelMapper.map(entity, AtendimentoResponse.class);
        response.setProntuarioId(prontuarioId);
        return response;
    }
}
