package com.proarte.erp.catalogos.repository;

import com.proarte.erp.catalogos.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EstadoRepository extends JpaRepository<Estado, UUID> {

    List<Estado> findByContexto(String contexto);
}
