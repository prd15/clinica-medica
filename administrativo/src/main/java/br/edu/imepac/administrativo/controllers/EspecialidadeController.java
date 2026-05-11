package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.EspecialidadeRequest;
import br.edu.imepac.administrativo.dtos.EspecialidadeResponse;
import br.edu.imepac.commons.entities.EspecialidadeEntity;
import br.edu.imepac.commons.services.EspecialidadeService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;
    private final ModelMapper modelMapper;

    public EspecialidadeController(EspecialidadeService especialidadeService, ModelMapper modelMapper) {
        this.especialidadeService = especialidadeService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<EspecialidadeResponse>> findAll() {
        List<EspecialidadeResponse> response = especialidadeService.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, EspecialidadeResponse.class))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadeResponse> findById(@PathVariable("id") Long id) {
        return especialidadeService.findById(id)
                .map(entity -> ResponseEntity.ok(modelMapper.map(entity, EspecialidadeResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EspecialidadeResponse> create(@Valid @RequestBody EspecialidadeRequest request) {
        EspecialidadeEntity entity = modelMapper.map(request, EspecialidadeEntity.class);
        EspecialidadeEntity saved = especialidadeService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(saved, EspecialidadeResponse.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadeResponse> update(@PathVariable("id") Long id,
                                                        @Valid @RequestBody EspecialidadeRequest request) {
        EspecialidadeEntity entity = modelMapper.map(request, EspecialidadeEntity.class);
        return especialidadeService.update(id, entity)
                .map(updated -> ResponseEntity.ok(modelMapper.map(updated, EspecialidadeResponse.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (especialidadeService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
