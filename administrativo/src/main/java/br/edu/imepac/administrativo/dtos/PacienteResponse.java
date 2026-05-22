package br.edu.imepac.administrativo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Dados retornados de um paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResponse {

    @Schema(description = "ID do paciente", example = "1")
    private Long id;

    @Schema(description = "Nome completo do paciente", example = "Joao da Silva")
    private String nome;

    @Schema(description = "CPF do paciente", example = "123.456.789-00")
    private String cpf;

    @Schema(description = "Data de nascimento do paciente", example = "1990-05-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    @Schema(description = "Telefone de contato", example = "(34) 99999-8888")
    private String telefone;

    @Schema(description = "E-mail do paciente", example = "joao@email.com")
    private String email;

    @Schema(description = "Endereco do paciente", example = "Rua das Flores, 123")
    private String endereco;

    @Schema(description = "ID do convenio do paciente", example = "1")
    private Long convenioId;

    @Schema(description = "Data e hora de criacao do registro", example = "2026-05-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da ultima atualizacao do registro", example = "2026-05-22T11:00:00")
    private LocalDateTime updatedAt;
}
