package br.edu.imepac.administrativo.convenio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConvenioRepository extends JpaRepository<ConvenioEntity, Long> {

    List<ConvenioEntity> findByAtivo(Boolean ativo);

    Optional<ConvenioEntity> findByNomeIgnoreCase(String nome);

    List<ConvenioEntity> findByNomeContainingIgnoreCase(String nome);
}
