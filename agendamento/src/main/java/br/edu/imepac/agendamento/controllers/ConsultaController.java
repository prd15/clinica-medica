package br.edu.imepac.agendamento.controllers;

import br.edu.imepac.agendamento.clients.AdministrativoClient;
import br.edu.imepac.agendamento.dtos.ConsultaRequest;
import br.edu.imepac.agendamento.dtos.ConsultaResponse;
import br.edu.imepac.agendamento.dtos.ReagendarRequest;
import br.edu.imepac.commons.entities.ConsultaEntity;
import br.edu.imepac.commons.services.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Consultas", description = "Agendamento e gestao de consultas medicas")
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
}
