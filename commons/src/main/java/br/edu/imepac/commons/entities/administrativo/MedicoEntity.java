package br.edu.imepac.commons.entities.administrativo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
// exclui senha do toString — nao deixa a senha aparecer em logs
@ToString(exclude = "senha")
@Entity
@Table(name = "medicos")
public class MedicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do médico é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O CRM é obrigatório")
    @Column(nullable = false, unique = true, length = 20)
    private String crm;

    // senha nunca vai sair numa resposta JSON — nem que o controller retorne a entity por engano
    @JsonIgnore
    @NotBlank(message = "A senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "medico_especialidade",
        joinColumns = @JoinColumn(name = "medico_id"),
        inverseJoinColumns = @JoinColumn(name = "especialidade_id")
    )
    private Set<EspecialidadeEntity> especialidades = new HashSet<>();
}
