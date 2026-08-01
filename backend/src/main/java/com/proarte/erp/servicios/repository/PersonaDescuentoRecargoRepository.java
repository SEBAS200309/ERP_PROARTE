package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.PersonaDescuentoRecargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonaDescuentoRecargoRepository extends JpaRepository<PersonaDescuentoRecargo, UUID> {

    boolean existsByPersonaIdAndDescuentoRecargoId(UUID personaId, UUID descuentoRecargoId);
}
