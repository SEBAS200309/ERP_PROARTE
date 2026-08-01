package com.proarte.erp.catalogos.repository;

import com.proarte.erp.catalogos.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, UUID> {
}
