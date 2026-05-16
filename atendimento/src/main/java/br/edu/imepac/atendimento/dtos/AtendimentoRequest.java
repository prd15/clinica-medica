package br.edu.imepac.atendimento.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoRequest {

    @NotNull(message = "consultaId e obrigatorio")
    private Long consultaId;

    @NotNull(message = "medicoId e obrigatorio")
    private Long medicoId;

    @NotNull(message = "pacienteId e obrigatorio")
    private Long pacienteId;

    @NotBlank(message = "descricao e obrigatoria")
    private String descricao;

    @NotBlank(message = "diagnostico e obrigatorio")
    private String diagnostico;

    private String observacoes;
}
