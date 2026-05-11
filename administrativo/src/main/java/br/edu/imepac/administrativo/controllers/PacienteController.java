package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.PacienteRequest;
import br.edu.imepac.administrativo.dtos.PacienteResponse;
import br.edu.imepac.commons.entities.PacienteEntity;
import br.edu.imepac.commons.services.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pacientes", description = "Gerenciamento de pacientes da clinica")
@RestController
@RequestMapping("/v1/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final ModelMapper modelMapper;

    public PacienteController(PacienteService pacienteService, ModelMapper modelMapper) {
        this.pacienteService = pacienteService;
        this.modelMapper = modelMapper;
    }

    // todos os params sao opcionais — sem filtro retorna tudo
    @Operation(summary = "Lista pacientes com filtros opcionais por nome, CPF ou convenio")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar(
            @RequestParam(required = false)
            @Parameter(description = "Filtro por nome (busca parcial, case-insensitive)")
            String nome,

            @RequestParam(required = false)
            @Parameter(description = "Filtro por CPF (busca exata)")
            String cpf,

            @RequestParam(required = false)
            @Parameter(description = "Filtro por ID do convenio")
            Long convenioId) {
        List<PacienteResponse> response = pacienteService.buscarComFiltros(nome, cpf, convenioId)
                .stream()
                .map(entity -> modelMapper.map(entity, PacienteResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busca paciente por ID")
    @ApiResponse(responseCode = "200", description = "Paciente encontrado")
    @ApiResponse(responseCode = "404", description = "Paciente nao encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> findById(@PathVariable("id") Long id) {
        return pacienteService.findById(id)
                .map(entity -> ResponseEntity.ok(modelMapper.map(entity, PacienteResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cria novo paciente")
    @ApiResponse(responseCode = "201", description = "Paciente criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @PostMapping
    public ResponseEntity<PacienteResponse> create(@Valid @RequestBody PacienteRequest request) {
        PacienteEntity entity = modelMapper.map(request, PacienteEntity.class);
        PacienteEntity saved = pacienteService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(saved, PacienteResponse.class));
    }

    @Operation(summary = "Atualiza paciente existente")
    @ApiResponse(responseCode = "200", description = "Paciente atualizado")
    @ApiResponse(responseCode = "404", description = "Paciente nao encontrado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> update(@PathVariable("id") Long id,
                                                   @Valid @RequestBody PacienteRequest request) {
        PacienteEntity entity = modelMapper.map(request, PacienteEntity.class);
        return pacienteService.update(id, entity)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, PacienteResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remove paciente por ID")
    @ApiResponse(responseCode = "204", description = "Paciente removido")
    @ApiResponse(responseCode = "404", description = "Paciente nao encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (pacienteService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
