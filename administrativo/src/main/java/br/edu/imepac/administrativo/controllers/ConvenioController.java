package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.AlterarStatusRequest;
import br.edu.imepac.administrativo.dtos.ConvenioRequest;
import br.edu.imepac.administrativo.dtos.ConvenioResponse;
import br.edu.imepac.commons.entities.administrativo.ConvenioEntity;
import br.edu.imepac.commons.services.administrativo.ConvenioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Convenios", description = "Gerenciamento de convenios da clinica")
@RestController
@RequestMapping("/v1/convenios")
public class ConvenioController {

    private final ConvenioService convenioService;
    private final ModelMapper modelMapper;

    public ConvenioController(ConvenioService convenioService, ModelMapper modelMapper) {
        this.convenioService = convenioService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Lista todos os convenios")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<ConvenioResponse>> findAll() {
        List<ConvenioResponse> response = convenioService.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, ConvenioResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busca convenio por ID")
    @ApiResponse(responseCode = "200", description = "Convenio encontrado")
    @ApiResponse(responseCode = "404", description = "Convenio nao encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<ConvenioResponse> findById(@PathVariable("id") Long id) {
        return convenioService.findById(id)
                .map(entity -> ResponseEntity.ok(modelMapper.map(entity, ConvenioResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cria novo convenio")
    @ApiResponse(responseCode = "201", description = "Convenio criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @PostMapping
    public ResponseEntity<ConvenioResponse> create(@Valid @RequestBody ConvenioRequest request) {
        ConvenioEntity entity = modelMapper.map(request, ConvenioEntity.class);
        ConvenioEntity saved = convenioService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(saved, ConvenioResponse.class));
    }

    @Operation(summary = "Atualiza convenio existente")
    @ApiResponse(responseCode = "200", description = "Convenio atualizado")
    @ApiResponse(responseCode = "404", description = "Convenio nao encontrado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @PutMapping("/{id}")
    public ResponseEntity<ConvenioResponse> update(@PathVariable("id") Long id,
                                                   @Valid @RequestBody ConvenioRequest request) {
        ConvenioEntity entity = modelMapper.map(request, ConvenioEntity.class);
        return convenioService.update(id, entity)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, ConvenioResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remove convenio por ID")
    @ApiResponse(responseCode = "204", description = "Convenio removido")
    @ApiResponse(responseCode = "404", description = "Convenio nao encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (convenioService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Ativa ou inativa um convenio")
    @ApiResponse(responseCode = "200", description = "Status alterado com sucesso")
    @ApiResponse(responseCode = "404", description = "Convenio nao encontrado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ConvenioResponse> alterarStatus(@PathVariable("id") Long id,
                                                          @Valid @RequestBody AlterarStatusRequest request) {
        ConvenioEntity atualizado = convenioService.alterarStatus(id, request.getAtivo());
        return ResponseEntity.ok(modelMapper.map(atualizado, ConvenioResponse.class));
    }
}

