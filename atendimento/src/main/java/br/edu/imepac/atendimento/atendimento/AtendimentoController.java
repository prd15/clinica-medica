package br.edu.imepac.atendimento.atendimento;

import br.edu.imepac.atendimento.atendimento.dto.AtendimentoRequest;
import br.edu.imepac.atendimento.atendimento.dto.AtendimentoResponse;
import br.edu.imepac.atendimento.atendimento.dto.HistoricoResponse;
import br.edu.imepac.atendimento.integration.agendamento.AgendamentoClient;
import br.edu.imepac.atendimento.integration.agendamento.dto.ConsultaRefDTO;
import br.edu.imepac.atendimento.integration.agendamento.dto.StatusConsulta;
import br.edu.imepac.atendimento.prontuario.ProntuarioEntity;
import br.edu.imepac.atendimento.prontuario.ProntuarioService;
import br.edu.imepac.commons.exceptions.BusinessException;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/atendimentos")
@Tag(name = "Atendimentos", description = "Registro clínico de consultas realizadas")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;
    private final ProntuarioService prontuarioService;
    private final AgendamentoClient agendamentoClient;
    private final ModelMapper modelMapper;

    public AtendimentoController(AtendimentoService atendimentoService,
                                 ProntuarioService prontuarioService,
                                 AgendamentoClient agendamentoClient,
                                 ModelMapper modelMapper) {
        this.atendimentoService = atendimentoService;
        this.prontuarioService = prontuarioService;
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

        Long prontuarioId = prontuarioService.buscarProntuario(salvo.getId()).getId();

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
}
