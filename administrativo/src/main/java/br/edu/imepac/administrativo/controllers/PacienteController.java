package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.PacienteRequest;
import br.edu.imepac.administrativo.dtos.PacienteResponse;
import br.edu.imepac.commons.entities.PacienteEntity;
import br.edu.imepac.commons.services.PacienteService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final ModelMapper modelMapper;

    public PacienteController(PacienteService pacienteService, ModelMapper modelMapper) {
        this.pacienteService = pacienteService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> findAll() {
        List<PacienteResponse> response = pacienteService.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, PacienteResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> findById(@PathVariable Long id) {
        return pacienteService.findById(id)
                .map(entity -> ResponseEntity.ok(modelMapper.map(entity, PacienteResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> create(@Valid @RequestBody PacienteRequest request) {
        PacienteEntity entity = modelMapper.map(request, PacienteEntity.class);
        PacienteEntity saved = pacienteService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(saved, PacienteResponse.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody PacienteRequest request) {
        PacienteEntity entity = modelMapper.map(request, PacienteEntity.class);
        return pacienteService.update(id, entity)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, PacienteResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (pacienteService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
