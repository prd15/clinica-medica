package br.edu.imepac.commons.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "atendentes")
public class AtendenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do atendente e obrigatorio")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O usuario do atendente e obrigatorio")
    @Column(nullable = false, unique = true, length = 80)
    private String usuario;

    // senha nunca sai em JSON — mesmo que o controller retorne a entity diretamente
    @JsonIgnore
    @NotBlank(message = "A senha do atendente e obrigatoria")
    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private Boolean ativo = true;
}
