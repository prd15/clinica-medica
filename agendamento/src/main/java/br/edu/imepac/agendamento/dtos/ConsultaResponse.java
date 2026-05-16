package br.edu.imepac.agendamento.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaResponse {

    private Long id;
    private Long pacienteId;
    private Long medicoId;
    private Long convenioId;
    private LocalDateTime dataHora;
    private String status;
    private String observacoes;
}
