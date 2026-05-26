package br.edu.imepac.atendimento.controllers;

import br.edu.imepac.atendimento.clients.AgendamentoClient;
import br.edu.imepac.atendimento.clients.dto.ConsultaRefDTO;
import br.edu.imepac.atendimento.dtos.AnotacaoRequest;
import br.edu.imepac.atendimento.dtos.AnotacaoResponse;
import br.edu.imepac.atendimento.dtos.AtendimentoRequest;
import br.edu.imepac.atendimento.dtos.AtendimentoResponse;
import br.edu.imepac.atendimento.dtos.ExameRequest;
import br.edu.imepac.atendimento.dtos.ExameResponse;
import br.edu.imepac.atendimento.dtos.HistoricoResponse;
import br.edu.imepac.atendimento.dtos.ProntuarioResponse;
import br.edu.imepac.atendimento.anotacao.AnotacaoEntity;
import br.edu.imepac.atendimento.atendimento.AtendimentoEntity;
import br.edu.imepac.atendimento.atendimento.AtendimentoService;
import br.edu.imepac.atendimento.clients.dto.StatusConsulta;
import br.edu.imepac.atendimento.exame.SolicitacaoExameEntity;
import br.edu.imepac.atendimento.prontuario.ProntuarioEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.imepac.commons.exceptions.BusinessException;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;

import java.util.List;

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
    @Operation(summary = "Realiza atendimento e registra prontuário")
    @ApiResponse(responseCode = "201", description = "Atendimento registrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Consulta nao encontrada no agendamento")
    @ApiResponse(responseCode = "409", description = "Consulta cancelada/realizada ou ja atendida")
    public ResponseEntity<AtendimentoResponse> realizar(@RequestBody @Valid AtendimentoRequest request) {
        // valida no agendamento antes de salvar: a consulta precisa existir e nao estar cancelada/realizada
        // se o agendamento esta offline, AgendamentoClient lanca ServicoIndisponivelException (503),
        // distinto do 404 retornado quando a consulta realmente nao existe
        ConsultaRefDTO consulta = agendamentoClient.buscarConsulta(request.getConsultaId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Consulta", request.getConsultaId()));
        if (consulta.getStatus() == StatusConsulta.CANCELADA
                || consulta.getStatus() == StatusConsulta.REALIZADA) {
            throw new BusinessException(
                    "Consulta no status " + consulta.getStatus() + " nao pode gerar atendimento");
        }

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

        Long prontuarioId = atendimentoService.buscarProntuario(salvo.getId()).getId();

        // a notificacao ao agendamento nao e mais sincrona: registrar() gravou um evento
        // no outbox na mesma transacao, e o OutboxScheduler entrega com retry. Como o
        // atendimento foi salvo (201), o evento esta garantidamente enfileirado.
        AtendimentoResponse response = modelMapper.map(salvo, AtendimentoResponse.class);
        response.setProntuarioId(prontuarioId);
        response.setConsultaAtualizada(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/historico")
    @Operation(summary = "Retorna histórico de atendimentos de um paciente")
    @ApiResponse(responseCode = "200", description = "Histórico retornado")
    @ApiResponse(responseCode = "400", description = "ID do paciente ausente ou em formato invalido")
    public ResponseEntity<List<HistoricoResponse>> historico(@RequestParam Long pacienteId) {
        List<HistoricoResponse> response = atendimentoService.buscarHistoricoPorPaciente(pacienteId)
                .stream()
                .map(entity -> modelMapper.map(entity, HistoricoResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{consultaId}")
    @Operation(summary = "Retorna o prontuário de uma consulta específica")
    @ApiResponse(responseCode = "200", description = "Prontuário encontrado")
    @ApiResponse(responseCode = "400", description = "ID da consulta em formato invalido")
    @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    public ResponseEntity<ProntuarioResponse> prontuarioPorConsulta(@PathVariable Long consultaId) {
        AtendimentoEntity atendimento = atendimentoService.buscarPorConsulta(consultaId);
        ProntuarioEntity prontuario = atendimentoService.buscarProntuario(atendimento.getId());
        return ResponseEntity.ok(modelMapper.map(prontuario, ProntuarioResponse.class));
    }

    @PostMapping("/{id}/anotacoes")
    @Operation(summary = "Adiciona anotação ao prontuário do atendimento")
    @ApiResponse(responseCode = "201", description = "Anotação registrada")
    @ApiResponse(responseCode = "400", description = "ID invalido ou dados invalidos")
    @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    public ResponseEntity<AnotacaoResponse> adicionarAnotacao(@PathVariable Long id,
                                                              @RequestBody @Valid AnotacaoRequest request) {
        AnotacaoEntity anotacao = atendimentoService.adicionarAnotacao(id, request.getTexto());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(anotacao, AnotacaoResponse.class));
    }

    @PostMapping("/{id}/exames")
    @Operation(summary = "Solicita exame vinculado ao atendimento")
    @ApiResponse(responseCode = "201", description = "Exame solicitado")
    @ApiResponse(responseCode = "400", description = "ID invalido ou dados invalidos")
    @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    public ResponseEntity<ExameResponse> solicitarExame(@PathVariable Long id,
                                                        @RequestBody @Valid ExameRequest request) {
        SolicitacaoExameEntity exame = atendimentoService.solicitarExame(id, request.getDescricao(), request.getTipo());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(exame, ExameResponse.class));
    }

    @GetMapping("/{id}/anotacoes")
    @Operation(summary = "Lista anotacoes do prontuario do atendimento")
    @ApiResponse(responseCode = "200", description = "Anotacoes retornadas")
    @ApiResponse(responseCode = "400", description = "ID informado em formato invalido")
    @ApiResponse(responseCode = "404", description = "Atendimento ou prontuario nao encontrado")
    public ResponseEntity<List<AnotacaoResponse>> listarAnotacoes(@PathVariable Long id) {
        List<AnotacaoResponse> response = atendimentoService.listarAnotacoes(id)
                .stream()
                .map(entity -> modelMapper.map(entity, AnotacaoResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/exames")
    @Operation(summary = "Lista exames solicitados no atendimento")
    @ApiResponse(responseCode = "200", description = "Exames retornados")
    @ApiResponse(responseCode = "400", description = "ID informado em formato invalido")
    @ApiResponse(responseCode = "404", description = "Atendimento nao encontrado")
    public ResponseEntity<List<ExameResponse>> listarExames(@PathVariable Long id) {
        List<ExameResponse> response = atendimentoService.listarExames(id)
                .stream()
                .map(entity -> modelMapper.map(entity, ExameResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }
}
