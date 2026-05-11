package br.edu.imepac.administrativo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicoResponse {

    private Long id;
    private String nome;
    private String crm;
    private String email;
    private String telefone;
    private Boolean ativo;
    private Set<EspecialidadeResponse> especialidades;
}
