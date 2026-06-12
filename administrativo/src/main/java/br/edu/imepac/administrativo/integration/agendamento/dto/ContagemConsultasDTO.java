package br.edu.imepac.administrativo.integration.agendamento.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

// projecao do endpoint /v1/consultas/contagem do agendamento — so o total
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContagemConsultasDTO {

    private long total;
}
