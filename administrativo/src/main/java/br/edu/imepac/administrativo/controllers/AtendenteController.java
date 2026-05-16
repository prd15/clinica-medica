package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.AtendenteRequest;
import br.edu.imepac.administrativo.dtos.AtendenteResponse;
import br.edu.imepac.commons.entities.AtendenteEntity;
import br.edu.imepac.commons.services.AtendenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// controller de atendentes — senha nunca aparece em AtendenteResponse
// usuario duplicado retorna 409, mesmo padrao do MedicoController
@Tag(name = "Atendentes", description = "Gerenciamento de atendentes e recepcionistas")
@RestController
@RequestMapping("/v1/atendentes")
public class AtendenteController {

    private final AtendenteService atendenteService;
    private final ModelMapper modelMapper;

    public AtendenteController(AtendenteService atendenteService, ModelMapper modelMapper) {
        this.atendenteService = atendenteService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Cria novo atendente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Atendente criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados invalidos"),
        @ApiResponse(responseCode = "409", description = "Usuario ja cadastrado")
    })
    @PostMapping
    public ResponseEntity<AtendenteResponse> create(@Valid @RequestBody AtendenteRequest request) {
        AtendenteEntity entity = modelMapper.map(request, AtendenteEntity.class);
        AtendenteEntity saved = atendenteService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(saved, AtendenteResponse.class));
    }

    @Operation(summary = "Lista todos os atendentes")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<AtendenteResponse>> findAll() {
        List<AtendenteResponse> response = atendenteService.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, AtendenteResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualiza atendente existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Atendente atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados invalidos"),
        @ApiResponse(responseCode = "404", description = "Atendente nao encontrado"),
        @ApiResponse(responseCode = "409", description = "Usuario ja cadastrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AtendenteResponse> update(@PathVariable("id") Long id,
                                                    @Valid @RequestBody AtendenteRequest request) {
        AtendenteEntity entity = modelMapper.map(request, AtendenteEntity.class);
        return atendenteService.update(id, entity)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, AtendenteResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remove atendente por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Atendente removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Atendente nao encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (atendenteService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
