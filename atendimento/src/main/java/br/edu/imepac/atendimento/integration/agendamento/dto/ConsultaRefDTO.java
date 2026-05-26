package br.edu.imepac.atendimento.integration.agendamento.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// projecao minima da consulta que o atendimento precisa do agendamento — id e status
// status tipado como enum evita typos do tipo "CANCELLADA" no controller
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsultaRefDTO {

    private Long id;
    private StatusConsulta status;
}
