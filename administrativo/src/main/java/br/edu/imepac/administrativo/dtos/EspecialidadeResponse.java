package br.edu.imepac.administrativo.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Dados retornados de uma especialidade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadeResponse {

    @Schema(description = "ID da especialidade", example = "1")
    private Long id;

    @Schema(description = "Nome da especialidade", example = "Cardiologia")
    private String nome;

    @Schema(description = "Descricao da especialidade", example = "Especialidade do coracao e sistema cardiovascular")
    private String descricao;

    @Schema(description = "Data e hora de criacao do registro", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da ultima atualizacao do registro", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;
}
