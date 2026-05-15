package br.edu.imepac.atendimento.controllers;

import br.edu.imepac.atendimento.clients.AgendamentoClient;
import br.edu.imepac.atendimento.dtos.AnotacaoRequest;
import br.edu.imepac.atendimento.dtos.AtendimentoRequest;
import br.edu.imepac.atendimento.dtos.AtendimentoResponse;
import br.edu.imepac.atendimento.dtos.ExameRequest;
import br.edu.imepac.commons.entities.AnotacaoEntity;
import br.edu.imepac.commons.entities.AtendimentoEntity;
import br.edu.imepac.commons.entities.ProntuarioEntity;
import br.edu.imepac.commons.entities.SolicitacaoExameEntity;
import br.edu.imepac.commons.services.AtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/atendimentos")
@Tag(name = "Atendimentos", description = "Registro clínico de consultas realizadas")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;
    private final AgendamentoClient agendamentoClient;

    public AtendimentoController(AtendimentoService atendimentoService,
                                 AgendamentoClient agendamentoClient) {
        this.atendimentoService = atendimentoService;
        this.agendamentoClient = agendamentoClient;
    }

    @PostMapping
    @Operation(summary = "Realiza atendimento e registra prontuário")
    @ApiResponse(responseCode = "201", description = "Atendimento registrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<AtendimentoResponse> realizar(@RequestBody @Valid AtendimentoRequest request) {
        AtendimentoEntity entidade = new AtendimentoEntity();
        entidade.setConsultaId(request.getConsultaId());
        entidade.setMedicoId(request.getMedicoId());
        entidade.setPacienteId(request.getPacienteId());

        AtendimentoEntity salvo = atendimentoService.registrar(
                entidade,
                request.getDescricao(),
                request.getDiagnostico(),
                request.getObservacoes()
        );

        try {
            agendamentoClient.confirmarRealizacao(request.getConsultaId());
        } catch (Exception e) {
            System.err.println("Aviso: nao foi possivel notificar o agendamento. " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo, null));
    }

    private AtendimentoResponse toResponse(AtendimentoEntity entidade, Long prontuarioId) {
        AtendimentoResponse r = new AtendimentoResponse();
        r.setId(entidade.getId());
        r.setConsultaId(entidade.getConsultaId());
        r.setMedicoId(entidade.getMedicoId());
        r.setPacienteId(entidade.getPacienteId());
        r.setDataHora(entidade.getDataHora());
        r.setStatus(entidade.getStatus());
        r.setProntuarioId(prontuarioId);
        return r;
    }
}
