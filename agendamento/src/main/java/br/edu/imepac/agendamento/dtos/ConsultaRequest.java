package br.edu.imepac.agendamento.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaRequest {

    @NotNull(message = "pacienteId e obrigatorio")
    private Long pacienteId;

    @NotNull(message = "medicoId e obrigatorio")
    private Long medicoId;

    @NotNull(message = "convenioId e obrigatorio")
    private Long convenioId;

    @NotNull(message = "dataHora e obrigatoria")
    private LocalDateTime dataHora;

    private String observacoes;
}
