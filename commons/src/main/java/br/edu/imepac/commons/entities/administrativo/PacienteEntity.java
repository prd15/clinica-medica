package br.edu.imepac.commons.entities.administrativo;

import br.edu.imepac.commons.entities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Paciente cadastrado na clinica.
 * Tabela: pacientes (banco clinica_administrativo)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pacientes")
public class PacienteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do paciente e obrigatorio")
    @Column(nullable = false, length = 200)
    private String nome;

    @NotBlank(message = "O CPF do paciente e obrigatorio")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    // nullable pq pacientes ja cadastrados antes dessa mudanca nao tem essa info
    @Column(nullable = true)
    private LocalDate dataNascimento;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 300)
    private String endereco;

    // id do convenio ao inves de FK — cada servico tem seu proprio banco
    private Long convenioId;
}
