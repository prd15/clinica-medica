package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.MedicoRequest;
import br.edu.imepac.administrativo.dtos.MedicoResponse;
import br.edu.imepac.commons.entities.MedicoEntity;
import br.edu.imepac.commons.services.MedicoService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/medicos")
public class MedicoController {

    private final MedicoService medicoService;
    private final ModelMapper modelMapper;

    public MedicoController(MedicoService medicoService, ModelMapper modelMapper) {
        this.medicoService = medicoService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponse>> findAll() {
        List<MedicoResponse> response = medicoService.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, MedicoResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> findById(@PathVariable("id") Long id) {
        return medicoService.findById(id)
                .map(entity -> ResponseEntity.ok(modelMapper.map(entity, MedicoResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MedicoResponse> create(@Valid @RequestBody MedicoRequest request) {
        MedicoEntity entity = modelMapper.map(request, MedicoEntity.class);
        MedicoEntity saved = medicoService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(saved, MedicoResponse.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponse> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody MedicoRequest request) {
        MedicoEntity entity = modelMapper.map(request, MedicoEntity.class);
        return medicoService.update(id, entity)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, MedicoResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (medicoService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
