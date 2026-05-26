package br.edu.imepac.administrativo.paciente.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 200, message = "Nome nao pode ter mais de 200 caracteres")
    private String nome;

    @Schema(description = "CPF do paciente", example = "123.456.789-00")
    @NotBlank(message = "O CPF do paciente e obrigatorio")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11}",
            message = "CPF deve estar no formato 000.000.000-00 ou 11 digitos")
    private String cpf;

    @Schema(description = "Data de nascimento do paciente", example = "1990-05-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @Schema(description = "Telefone de contato", example = "(34) 99999-8888")
    @Size(max = 20, message = "Telefone nao pode ter mais de 20 caracteres")
    private String telefone;

    @Schema(description = "E-mail do paciente", example = "joao@email.com")
    @Email(message = "E-mail invalido")
    @Size(max = 150, message = "E-mail nao pode ter mais de 150 caracteres")
    private String email;

    @Schema(description = "Endereco do paciente", example = "Rua das Flores, 123")
    @Size(max = 300, message = "Endereco nao pode ter mais de 300 caracteres")
    private String endereco;

    @Schema(description = "ID do convenio do paciente", example = "1")
    private Long convenioId;
}
