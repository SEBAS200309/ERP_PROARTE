package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.PersonaDescuentoRecargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonaDescuentoRecargoRepository extends JpaRepository<PersonaDescuentoRecargo, UUID> {

    boolean existsByPersonaIdAndDescuentoRecargoId(UUID personaId, UUID descuentoRecargoId);
}
