package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.MedicoEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<MedicoEntity, Long> {

    Optional<MedicoEntity> findByCrm(String crm);

    // EntityGraph faz join fetch das especialidades em uma unica query — evita N+1
    @Override
    @EntityGraph(attributePaths = "especialidades")
    List<MedicoEntity> findAll();

    @EntityGraph(attributePaths = "especialidades")
    List<MedicoEntity> findByAtivo(Boolean ativo);
}
