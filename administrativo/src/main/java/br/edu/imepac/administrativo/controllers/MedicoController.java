package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.MedicoRequest;
import br.edu.imepac.administrativo.dtos.MedicoResponse;
import br.edu.imepac.commons.entities.MedicoEntity;
import br.edu.imepac.commons.services.MedicoService;
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

// controller de medicos — leitura/escrita via MedicoService, mapeamento com ModelMapper
// senha nunca entra em MedicoResponse, nem chega a passar pelo mapper
@Tag(name = "Médicos", description = "Gerenciamento de médicos")
@RestController
@RequestMapping("/v1/medicos")
public class MedicoController {

    private final MedicoService medicoService;
    private final ModelMapper modelMapper;

    public MedicoController(MedicoService medicoService, ModelMapper modelMapper) {
        this.medicoService = medicoService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Lista todos os médicos")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<MedicoResponse>> findAll() {
        List<MedicoResponse> response = medicoService.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, MedicoResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    // filtra por ativo=true (padrao) ou ativo=false para ver medicos desligados
    @Operation(summary = "Lista médicos por status ativo")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping("/ativos")
    public ResponseEntity<List<MedicoResponse>> findByAtivo(@RequestParam(defaultValue = "true") Boolean ativo) {
        List<MedicoResponse> response = medicoService.findByAtivo(ativo)
                .stream()
                .map(entity -> modelMapper.map(entity, MedicoResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busca médico por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Médico encontrado"),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> findById(@PathVariable("id") Long id) {
        return medicoService.findById(id)
                .map(entity -> ResponseEntity.ok(modelMapper.map(entity, MedicoResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 409 quando CRM ja esta em uso — servico lanca IllegalStateException nesses casos
    @Operation(summary = "Cria novo médico")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Médico criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "CRM já cadastrado")
    })
    @PostMapping
    public ResponseEntity<MedicoResponse> create(@Valid @RequestBody MedicoRequest request) {
        // IllegalStateException (CRM duplicado) tratada pelo GlobalExceptionHandler → 409
        MedicoEntity entity = modelMapper.map(request, MedicoEntity.class);
        MedicoEntity saved = medicoService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(saved, MedicoResponse.class));
    }

    @Operation(summary = "Atualiza médico existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Médico atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado"),
        @ApiResponse(responseCode = "409", description = "CRM já cadastrado para outro médico")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponse> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody MedicoRequest request) {
        try {
            MedicoEntity entity = modelMapper.map(request, MedicoEntity.class);
            return medicoService.update(id, entity)
                    .map(updated -> ResponseEntity.ok(modelMapper.map(updated, MedicoResponse.class)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Associa especialidade ao médico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Associado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Médico ou especialidade não encontrado"),
        @ApiResponse(responseCode = "409", description = "Especialidade já associada ao médico")
    })
    @PostMapping("/{id}/especialidades/{especialidadeId}")
    public ResponseEntity<MedicoResponse> associarEspecialidade(@PathVariable("id") Long id,
                                                                 @PathVariable("especialidadeId") Long especialidadeId) {
        try {
            return medicoService.associarEspecialidade(id, especialidadeId)
                    .map(updated -> ResponseEntity.ok(modelMapper.map(updated, MedicoResponse.class)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Remove especialidade do médico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Médico ou especialidade não encontrado")
    })
    @DeleteMapping("/{id}/especialidades/{especialidadeId}")
    public ResponseEntity<MedicoResponse> removerEspecialidade(@PathVariable("id") Long id,
                                                                @PathVariable("especialidadeId") Long especialidadeId) {
        return medicoService.removerEspecialidade(id, especialidadeId)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, MedicoResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    // soft delete — nao remove do banco, so marca ativo=false
    @Operation(summary = "Inativa médico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Médico inativado"),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    })
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<MedicoResponse> inativar(@PathVariable("id") Long id) {
        return medicoService.inativar(id)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, MedicoResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remove médico por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Médico removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (medicoService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
