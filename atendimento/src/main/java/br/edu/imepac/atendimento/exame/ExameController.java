package br.edu.imepac.atendimento.exame;

import br.edu.imepac.atendimento.exame.dto.ExameRequest;
import br.edu.imepac.atendimento.exame.dto.ExameResponse;
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
@Tag(name = "Exames", description = "Solicitacoes de exames medicos")
public class ExameController {

    private final ExameService exameService;
    private final ModelMapper modelMapper;

    public ExameController(ExameService exameService, ModelMapper modelMapper) {
        this.exameService = exameService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/{id}/exames")
    @Operation(summary = "Solicita exame vinculado ao atendimento")
    @ApiResponse(responseCode = "201", description = "Exame solicitado")
    @ApiResponse(responseCode = "400", description = "ID invalido ou dados invalidos")
    @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    public ResponseEntity<ExameResponse> solicitarExame(@PathVariable Long id,
                                                        @RequestBody @Valid ExameRequest request) {
        SolicitacaoExameEntity exame = exameService.solicitarExame(id, request.getDescricao(), request.getTipo());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(exame, ExameResponse.class));
    }

    @GetMapping("/{id}/exames")
    @Operation(summary = "Lista exames solicitados no atendimento")
    @ApiResponse(responseCode = "200", description = "Exames retornados")
    @ApiResponse(responseCode = "400", description = "ID informado em formato invalido")
    @ApiResponse(responseCode = "404", description = "Atendimento nao encontrado")
    public ResponseEntity<List<ExameResponse>> listarExames(@PathVariable Long id) {
        List<ExameResponse> response = exameService.listarExames(id)
                .stream()
                .map(entity -> modelMapper.map(entity, ExameResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }
}
