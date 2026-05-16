package br.edu.imepac.agendamento.controllers;

import br.edu.imepac.agendamento.clients.AdministrativoClient;
import br.edu.imepac.agendamento.dtos.ConsultaRequest;
import br.edu.imepac.agendamento.dtos.ConsultaResponse;
import br.edu.imepac.commons.entities.ConsultaEntity;
import br.edu.imepac.commons.services.ConsultaService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final AdministrativoClient administrativoClient;
    private final ModelMapper modelMapper;

    public ConsultaController(ConsultaService consultaService,
                              AdministrativoClient administrativoClient,
                              ModelMapper modelMapper) {
        this.consultaService = consultaService;
        this.administrativoClient = administrativoClient;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<Object> agendar(@Valid @RequestBody ConsultaRequest request) {
        if (!administrativoClient.isConvenioAtivo(request.getConvenioId())) {
            return ResponseEntity.badRequest().body("Convenio inativo ou nao encontrado");
        }
        if (!administrativoClient.isMedicoAtivo(request.getMedicoId())) {
            return ResponseEntity.badRequest().body("Medico inativo ou nao encontrado");
        }
        if (!administrativoClient.isPacienteExistente(request.getPacienteId())) {
            return ResponseEntity.badRequest().body("Paciente nao encontrado");
        }

        ConsultaEntity entidade = modelMapper.map(request, ConsultaEntity.class);
        ConsultaEntity salva = consultaService.agendar(entidade);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(salva, ConsultaResponse.class));
    }
}
