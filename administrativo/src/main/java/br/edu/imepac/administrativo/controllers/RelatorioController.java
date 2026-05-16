package br.edu.imepac.administrativo.controllers;

import br.edu.imepac.administrativo.dtos.ConsultaDiariaRelatorioResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/relatorios")
public class RelatorioController {

    // endpoint pendente de integracao com o microsservico de agendamento
    // oculto no Swagger para nao iludir consumidores com dado zerado
    @Hidden
    @Operation(summary = "Relatorio de consultas diarias (pendente integracao com agendamento)")
    @ApiResponse(responseCode = "200", description = "Relatorio retornado com sucesso")
    @GetMapping("/consultas-diarias")
    public ResponseEntity<ConsultaDiariaRelatorioResponse> consultasDiarias() {
        ConsultaDiariaRelatorioResponse response = new ConsultaDiariaRelatorioResponse(0L, LocalDate.now().toString());
        return ResponseEntity.ok(response);
    }
}
