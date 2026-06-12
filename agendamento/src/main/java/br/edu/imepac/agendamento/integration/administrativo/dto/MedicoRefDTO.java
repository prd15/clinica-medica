package br.edu.imepac.agendamento.integration.administrativo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicoRefDTO {

    private Long id;
    private String nome;
    private Boolean ativo;
}
