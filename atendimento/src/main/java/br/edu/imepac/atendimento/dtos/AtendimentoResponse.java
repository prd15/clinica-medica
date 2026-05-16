package br.edu.imepac.atendimento.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoResponse {

    private Long id;
    private Long consultaId;
    private Long medicoId;
    private Long pacienteId;
    private LocalDateTime dataHora;
    private String descricao;
    private String diagnostico;
    private String observacoes;
    private Long prontuarioId;
}
