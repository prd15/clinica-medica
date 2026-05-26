package br.edu.imepac.administrativo.relatorio.dto;

import br.edu.imepac.administrativo.paciente.dto.PacienteResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Relatorio de pacientes vinculados a um convenio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacientesPorConvenioRelatorioResponse {

    @Schema(description = "ID do convenio consultado", example = "1")
    private Long convenioId;

    @Schema(description = "Total de pacientes vinculados ao convenio", example = "12")
    private Integer totalPacientes;

    @Schema(description = "Pacientes encontrados para o convenio")
    private List<PacienteResponse> pacientes;
}
