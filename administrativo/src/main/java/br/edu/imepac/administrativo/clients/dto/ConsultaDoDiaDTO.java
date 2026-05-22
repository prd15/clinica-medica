package br.edu.imepac.administrativo.clients.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// projecao minima da consulta retornada pelo agendamento — id apenas pra contar
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsultaDoDiaDTO {

    private Long id;
}
