package com.proarte.erp.personas.repository;

import com.proarte.erp.personas.entity.PersonaEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonaEmpresaRepository extends JpaRepository<PersonaEmpresa, UUID> {

    List<PersonaEmpresa> findByPersonaId(UUID personaId);

    List<PersonaEmpresa> findByEmpresaId(UUID empresaId);

    Optional<PersonaEmpresa> findByPersonaIdAndEmpresaId(UUID personaId, UUID empresaId);

    void deleteByPersonaIdAndEmpresaId(UUID personaId, UUID empresaId);
}
