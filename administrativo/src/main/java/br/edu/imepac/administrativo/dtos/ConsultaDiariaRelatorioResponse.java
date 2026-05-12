package br.edu.imepac.administrativo.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaDiariaRelatorioResponse {

    private LocalDate data;
    private Integer totalConsultas;
}
