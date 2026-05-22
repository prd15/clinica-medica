package br.edu.imepac.atendimento.clients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// projecao minima da consulta que o atendimento precisa do agendamento — id e status
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsultaRefDTO {

    private Long id;
    private String status;
}
