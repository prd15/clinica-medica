package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.AtendenteRequest;
import br.edu.imepac.administrativo.dtos.AtendenteResponse;
import br.edu.imepac.commons.entities.AtendenteEntity;
import br.edu.imepac.commons.services.AtendenteService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/atendentes")
public class AtendenteController {

    private final AtendenteService atendenteService;
    private final ModelMapper modelMapper;

    public AtendenteController(AtendenteService atendenteService, ModelMapper modelMapper) {
        this.atendenteService = atendenteService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<AtendenteResponse> create(@Valid @RequestBody AtendenteRequest request) {
        try {
            AtendenteEntity entity = modelMapper.map(request, AtendenteEntity.class);
            AtendenteEntity saved = atendenteService.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(modelMapper.map(saved, AtendenteResponse.class));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<AtendenteResponse>> findAll() {
        List<AtendenteResponse> response = atendenteService.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, AtendenteResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtendenteResponse> update(@PathVariable("id") Long id,
                                                    @Valid @RequestBody AtendenteRequest request) {
        try {
            AtendenteEntity entity = modelMapper.map(request, AtendenteEntity.class);
            return atendenteService.update(id, entity)
                    .map(updated -> ResponseEntity.ok(modelMapper.map(updated, AtendenteResponse.class)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (atendenteService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
