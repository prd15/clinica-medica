package br.edu.imepac.atendimento.prontuario;

import br.edu.imepac.atendimento.atendimento.AtendimentoEntity;
import br.edu.imepac.atendimento.atendimento.AtendimentoService;
import br.edu.imepac.atendimento.prontuario.dto.ProntuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/atendimentos")
@Tag(name = "Prontuarios", description = "Consulta de prontuarios clinicos")
public class ProntuarioController {

    private final AtendimentoService atendimentoService;
    private final ProntuarioService prontuarioService;
    private final ModelMapper modelMapper;

    public ProntuarioController(AtendimentoService atendimentoService,
                                ProntuarioService prontuarioService,
                                ModelMapper modelMapper) {
        this.atendimentoService = atendimentoService;
        this.prontuarioService = prontuarioService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{consultaId}")
    @Operation(summary = "Retorna o prontuário de uma consulta específica")
    @ApiResponse(responseCode = "200", description = "Prontuário encontrado")
    @ApiResponse(responseCode = "400", description = "ID da consulta em formato invalido")
    @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    public ResponseEntity<ProntuarioResponse> prontuarioPorConsulta(@PathVariable Long consultaId) {
        AtendimentoEntity atendimento = atendimentoService.buscarPorConsulta(consultaId);
        ProntuarioEntity prontuario = prontuarioService.buscarProntuario(atendimento.getId());
        return ResponseEntity.ok(modelMapper.map(prontuario, ProntuarioResponse.class));
    }
}
