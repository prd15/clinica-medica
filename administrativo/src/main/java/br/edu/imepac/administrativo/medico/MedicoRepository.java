package br.edu.imepac.administrativo.medico;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<MedicoEntity, Long> {

    @EntityGraph(attributePaths = "especialidades")
    Optional<MedicoEntity> findByCrm(String crm);

    // EntityGraph faz join fetch das especialidades em uma unica query — evita N+1
    @Override
    @EntityGraph(attributePaths = "especialidades")
    List<MedicoEntity> findAll();

    @Override
    @EntityGraph(attributePaths = "especialidades")
    Optional<MedicoEntity> findById(Long id);

    @EntityGraph(attributePaths = "especialidades")
    List<MedicoEntity> findByAtivo(Boolean ativo);
}
