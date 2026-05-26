package br.edu.imepac.atendimento.anotacao;

import br.edu.imepac.atendimento.anotacao.dto.AnotacaoRequest;
import br.edu.imepac.atendimento.anotacao.dto.AnotacaoResponse;
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
@Tag(name = "Anotacoes", description = "Anotacoes clinicas do prontuario")
public class AnotacaoController {

    private final AnotacaoService anotacaoService;
    private final ModelMapper modelMapper;

    public AnotacaoController(AnotacaoService anotacaoService, ModelMapper modelMapper) {
        this.anotacaoService = anotacaoService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/{id}/anotacoes")
    @Operation(summary = "Adiciona anotação ao prontuário do atendimento")
    @ApiResponse(responseCode = "201", description = "Anotação registrada")
    @ApiResponse(responseCode = "400", description = "ID invalido ou dados invalidos")
    @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    public ResponseEntity<AnotacaoResponse> adicionarAnotacao(@PathVariable Long id,
                                                              @RequestBody @Valid AnotacaoRequest request) {
        AnotacaoEntity anotacao = anotacaoService.adicionarAnotacao(id, request.getTexto());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(anotacao, AnotacaoResponse.class));
    }

    @GetMapping("/{id}/anotacoes")
    @Operation(summary = "Lista anotacoes do prontuario do atendimento")
    @ApiResponse(responseCode = "200", description = "Anotacoes retornadas")
    @ApiResponse(responseCode = "400", description = "ID informado em formato invalido")
    @ApiResponse(responseCode = "404", description = "Atendimento ou prontuario nao encontrado")
    public ResponseEntity<List<AnotacaoResponse>> listarAnotacoes(@PathVariable Long id) {
        List<AnotacaoResponse> response = anotacaoService.listarAnotacoes(id)
                .stream()
                .map(entity -> modelMapper.map(entity, AnotacaoResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }
}
