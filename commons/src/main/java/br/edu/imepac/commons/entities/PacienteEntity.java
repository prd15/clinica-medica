package br.edu.imepac.commons.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Paciente cadastrado na clinica.
 * Tabela: pacientes (banco clinica_administrativo)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pacientes")
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do paciente e obrigatorio")
    @Column(nullable = false, length = 200)
    private String nome;

    @NotBlank(message = "O CPF do paciente e obrigatorio")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = true)
    private LocalDate dataNascimento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 300)
    private String endereco;

    // referencia ao convenio — Long em vez de @ManyToOne pra nao cruzar bancos
    private Long convenioId;
}
