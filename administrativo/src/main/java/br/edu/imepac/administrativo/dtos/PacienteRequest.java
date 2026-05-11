package br.edu.imepac.administrativo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Dados para criacao ou atualizacao de paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteRequest {

    @Schema(description = "Nome completo do paciente", example = "Joao da Silva")
    @NotBlank(message = "O nome do paciente e obrigatorio")
    private String nome;

    @Schema(description = "CPF do paciente", example = "123.456.789-00")
    @NotBlank(message = "O CPF do paciente e obrigatorio")
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
}
