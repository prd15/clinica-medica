package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.EspecialidadeRequest;
import br.edu.imepac.administrativo.dtos.EspecialidadeResponse;
import br.edu.imepac.commons.entities.EspecialidadeEntity;
import br.edu.imepac.commons.services.EspecialidadeService;
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

@Tag(name = "Especialidades", description = "Gerenciamento de especialidades médicas")
@RestController
@RequestMapping("/v1/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;
    private final ModelMapper modelMapper;

    public EspecialidadeController(EspecialidadeService especialidadeService, ModelMapper modelMapper) {
        this.especialidadeService = especialidadeService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Lista todas as especialidades")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<EspecialidadeResponse>> findAll() {
        List<EspecialidadeResponse> response = especialidadeService.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, EspecialidadeResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busca especialidade por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Especialidade encontrada"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato invalido"),
        @ApiResponse(responseCode = "404", description = "Especialidade não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadeResponse> findById(@PathVariable("id") Long id) {
        return especialidadeService.findById(id)
                .map(entity -> ResponseEntity.ok(modelMapper.map(entity, EspecialidadeResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cria nova especialidade")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Especialidade criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EspecialidadeResponse> create(@Valid @RequestBody EspecialidadeRequest request) {
        EspecialidadeEntity entity = modelMapper.map(request, EspecialidadeEntity.class);
        EspecialidadeEntity saved = especialidadeService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(saved, EspecialidadeResponse.class));
    }

    @Operation(summary = "Atualiza especialidade existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Especialidade atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados invalidos"),
        @ApiResponse(responseCode = "404", description = "Especialidade não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadeResponse> update(@PathVariable("id") Long id,
                                                        @Valid @RequestBody EspecialidadeRequest request) {
        EspecialidadeEntity entity = modelMapper.map(request, EspecialidadeEntity.class);
        return especialidadeService.update(id, entity)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, EspecialidadeResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remove especialidade por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Especialidade removida com sucesso"),
        @ApiResponse(responseCode = "400", description = "ID informado em formato invalido"),
        @ApiResponse(responseCode = "404", description = "Especialidade não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (especialidadeService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
