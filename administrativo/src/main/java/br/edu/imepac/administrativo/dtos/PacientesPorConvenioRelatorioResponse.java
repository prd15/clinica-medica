package br.edu.imepac.administrativo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacientesPorConvenioRelatorioResponse {

    private Long convenioId;
    private Integer totalPacientes;
    private List<PacienteResponse> pacientes;
}
