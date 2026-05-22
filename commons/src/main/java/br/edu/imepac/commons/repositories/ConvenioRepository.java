package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.ConvenioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConvenioRepository extends JpaRepository<ConvenioEntity, Long> {

    // util pra filtrar so os ativos no dropdown do front, por exemplo
    List<ConvenioEntity> findByAtivo(Boolean ativo);

    // unicidade de nome — usado pelo validarNomeDisponivel no service
    Optional<ConvenioEntity> findByNomeIgnoreCase(String nome);

    // busca parcial case-insensitive para filtro do front
    List<ConvenioEntity> findByNomeContainingIgnoreCase(String nome);
}

