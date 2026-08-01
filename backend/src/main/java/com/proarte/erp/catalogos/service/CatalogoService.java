package com.proarte.erp.catalogos.service;

import com.proarte.erp.catalogos.dto.CreateCatalogoRequest;
import com.proarte.erp.catalogos.dto.CatalogoResponse;
import com.proarte.erp.catalogos.dto.EstadoResponse;
import com.proarte.erp.catalogos.dto.UnidadMedidaResponse;
import com.proarte.erp.catalogos.entity.*;
import com.proarte.erp.catalogos.repository.*;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final RolEntidadRepository rolEntidadRepository;
    private final EstadoRepository estadoRepository;
    private final CategoriaServicioRepository categoriaServicioRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;
    private final RolEventoRepository rolEventoRepository;

    @Transactional(readOnly = true)
    public List<?> getAll(String tipo, String contexto) {
        log.info("Fetching all catalog entries for tipo={}, contexto={}", tipo, contexto);

        return switch (tipo) {
            case "tipo-documento" -> tipoDocumentoRepository.findAll().stream()
                    .map(e -> new CatalogoResponse(e.getId(), e.getNombre()))
                    .toList();
            case "rol-entidad" -> rolEntidadRepository.findAll().stream()
                    .map(e -> new CatalogoResponse(e.getId(), e.getNombre()))
                    .toList();
            case "estado" -> {
                List<Estado> estados = (contexto != null && !contexto.isBlank())
                        ? estadoRepository.findByContexto(contexto)
                        : estadoRepository.findAll();
                yield estados.stream()
                        .map(e -> new EstadoResponse(e.getId(), e.getNombre(), e.getContexto()))
                        .toList();
            }
            case "categoria-servicio" -> categoriaServicioRepository.findAll().stream()
                    .map(e -> new CatalogoResponse(e.getId(), e.getNombre()))
                    .toList();
            case "unidad-medida" -> unidadMedidaRepository.findAll().stream()
                    .map(e -> new UnidadMedidaResponse(e.getId(), e.getNombre(), e.getAbreviatura()))
                    .toList();
            case "rol-evento" -> rolEventoRepository.findAll().stream()
                    .map(e -> new CatalogoResponse(e.getId(), e.getNombre()))
                    .toList();
            default -> throw new ResourceNotFoundException("Tipo de catálogo no válido");
        };
    }

    @Transactional(readOnly = true)
    public Object getById(String tipo, UUID id) {
        log.info("Fetching catalog entry tipo={}, id={}", tipo, id);

        return switch (tipo) {
            case "tipo-documento" -> {
                TipoDocumento entity = tipoDocumentoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento", "id", id));
                yield new CatalogoResponse(entity.getId(), entity.getNombre());
            }
            case "rol-entidad" -> {
                RolEntidad entity = rolEntidadRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("RolEntidad", "id", id));
                yield new CatalogoResponse(entity.getId(), entity.getNombre());
            }
            case "estado" -> {
                Estado entity = estadoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Estado", "id", id));
                yield new EstadoResponse(entity.getId(), entity.getNombre(), entity.getContexto());
            }
            case "categoria-servicio" -> {
                CategoriaServicio entity = categoriaServicioRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("CategoriaServicio", "id", id));
                yield new CatalogoResponse(entity.getId(), entity.getNombre());
            }
            case "unidad-medida" -> {
                UnidadMedida entity = unidadMedidaRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("UnidadMedida", "id", id));
                yield new UnidadMedidaResponse(entity.getId(), entity.getNombre(), entity.getAbreviatura());
            }
            case "rol-evento" -> {
                RolEvento entity = rolEventoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("RolEvento", "id", id));
                yield new CatalogoResponse(entity.getId(), entity.getNombre());
            }
            default -> throw new ResourceNotFoundException("Tipo de catálogo no válido");
        };
    }

    @Transactional
    public Object create(String tipo, CreateCatalogoRequest request) {
        log.info("Creating catalog entry tipo={}, nombre={}", tipo, request.nombre());

        return switch (tipo) {
            case "tipo-documento" -> {
                TipoDocumento entity = TipoDocumento.builder()
                        .nombre(request.nombre())
                        .build();
                TipoDocumento saved = tipoDocumentoRepository.save(entity);
                yield new CatalogoResponse(saved.getId(), saved.getNombre());
            }
            case "rol-entidad" -> {
                RolEntidad entity = RolEntidad.builder()
                        .nombre(request.nombre())
                        .build();
                RolEntidad saved = rolEntidadRepository.save(entity);
                yield new CatalogoResponse(saved.getId(), saved.getNombre());
            }
            case "estado" -> {
                Estado entity = Estado.builder()
                        .nombre(request.nombre())
                        .contexto(request.contexto())
                        .build();
                Estado saved = estadoRepository.save(entity);
                yield new EstadoResponse(saved.getId(), saved.getNombre(), saved.getContexto());
            }
            case "categoria-servicio" -> {
                CategoriaServicio entity = CategoriaServicio.builder()
                        .nombre(request.nombre())
                        .build();
                CategoriaServicio saved = categoriaServicioRepository.save(entity);
                yield new CatalogoResponse(saved.getId(), saved.getNombre());
            }
            case "unidad-medida" -> {
                UnidadMedida entity = UnidadMedida.builder()
                        .nombre(request.nombre())
                        .abreviatura(request.abreviatura())
                        .build();
                UnidadMedida saved = unidadMedidaRepository.save(entity);
                yield new UnidadMedidaResponse(saved.getId(), saved.getNombre(), saved.getAbreviatura());
            }
            case "rol-evento" -> {
                RolEvento entity = RolEvento.builder()
                        .nombre(request.nombre())
                        .build();
                RolEvento saved = rolEventoRepository.save(entity);
                yield new CatalogoResponse(saved.getId(), saved.getNombre());
            }
            default -> throw new ResourceNotFoundException("Tipo de catálogo no válido");
        };
    }

    @Transactional
    public Object update(String tipo, UUID id, CreateCatalogoRequest request) {
        log.info("Updating catalog entry tipo={}, id={}", tipo, id);

        return switch (tipo) {
            case "tipo-documento" -> {
                TipoDocumento entity = tipoDocumentoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("TipoDocumento", "id", id));
                entity.setNombre(request.nombre());
                TipoDocumento saved = tipoDocumentoRepository.save(entity);
                yield new CatalogoResponse(saved.getId(), saved.getNombre());
            }
            case "rol-entidad" -> {
                RolEntidad entity = rolEntidadRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("RolEntidad", "id", id));
                entity.setNombre(request.nombre());
                RolEntidad saved = rolEntidadRepository.save(entity);
                yield new CatalogoResponse(saved.getId(), saved.getNombre());
            }
            case "estado" -> {
                Estado entity = estadoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Estado", "id", id));
                entity.setNombre(request.nombre());
                if (request.contexto() != null) {
                    entity.setContexto(request.contexto());
                }
                Estado saved = estadoRepository.save(entity);
                yield new EstadoResponse(saved.getId(), saved.getNombre(), saved.getContexto());
            }
            case "categoria-servicio" -> {
                CategoriaServicio entity = categoriaServicioRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("CategoriaServicio", "id", id));
                entity.setNombre(request.nombre());
                CategoriaServicio saved = categoriaServicioRepository.save(entity);
                yield new CatalogoResponse(saved.getId(), saved.getNombre());
            }
            case "unidad-medida" -> {
                UnidadMedida entity = unidadMedidaRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("UnidadMedida", "id", id));
                entity.setNombre(request.nombre());
                if (request.abreviatura() != null) {
                    entity.setAbreviatura(request.abreviatura());
                }
                UnidadMedida saved = unidadMedidaRepository.save(entity);
                yield new UnidadMedidaResponse(saved.getId(), saved.getNombre(), saved.getAbreviatura());
            }
            case "rol-evento" -> {
                RolEvento entity = rolEventoRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("RolEvento", "id", id));
                entity.setNombre(request.nombre());
                RolEvento saved = rolEventoRepository.save(entity);
                yield new CatalogoResponse(saved.getId(), saved.getNombre());
            }
            default -> throw new ResourceNotFoundException("Tipo de catálogo no válido");
        };
    }

    @Transactional
    public void delete(String tipo, UUID id) {
        log.info("Deleting catalog entry tipo={}, id={}", tipo, id);

        try {
            switch (tipo) {
                case "tipo-documento" -> {
                    if (!tipoDocumentoRepository.existsById(id)) {
                        throw new ResourceNotFoundException("TipoDocumento", "id", id);
                    }
                    tipoDocumentoRepository.deleteById(id);
                }
                case "rol-entidad" -> {
                    if (!rolEntidadRepository.existsById(id)) {
                        throw new ResourceNotFoundException("RolEntidad", "id", id);
                    }
                    rolEntidadRepository.deleteById(id);
                }
                case "estado" -> {
                    if (!estadoRepository.existsById(id)) {
                        throw new ResourceNotFoundException("Estado", "id", id);
                    }
                    estadoRepository.deleteById(id);
                }
                case "categoria-servicio" -> {
                    if (!categoriaServicioRepository.existsById(id)) {
                        throw new ResourceNotFoundException("CategoriaServicio", "id", id);
                    }
                    categoriaServicioRepository.deleteById(id);
                }
                case "unidad-medida" -> {
                    if (!unidadMedidaRepository.existsById(id)) {
                        throw new ResourceNotFoundException("UnidadMedida", "id", id);
                    }
                    unidadMedidaRepository.deleteById(id);
                }
                case "rol-evento" -> {
                    if (!rolEventoRepository.existsById(id)) {
                        throw new ResourceNotFoundException("RolEvento", "id", id);
                    }
                    rolEventoRepository.deleteById(id);
                }
                default -> throw new ResourceNotFoundException("Tipo de catálogo no válido");
            }
        } catch (DataIntegrityViolationException e) {
            log.warn("Cannot delete catalog entry tipo={}, id={} — referenced by FK", tipo, id);
            throw new BusinessException("No se puede eliminar porque está en uso");
        }
    }
}
