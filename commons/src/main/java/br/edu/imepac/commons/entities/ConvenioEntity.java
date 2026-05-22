package br.edu.imepac.commons.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "convenios")
public class ConvenioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do convênio é obrigatório")
    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    // cnpj unico por convenio — dois convenios com o mesmo cnpj nao faz sentido
    @NotBlank(message = "CNPJ é obrigatório")
    @Column(unique = true, nullable = false, length = 18)
    private String cnpj;

    // telefone de contato, nao obrigatorio
    @Column(length = 20)
    private String telefone;

    // convenios inativos ficam no banco mas nao aparecem no agendamento
    @Column(nullable = false)
    private Boolean ativo = true;
}

