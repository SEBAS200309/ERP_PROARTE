-- ============================================================
-- V8: Funciones y Triggers — Lógica de negocio
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

-- ============================================================
-- Función: fn_recalcular_total_cotizacion
-- Descripción: Recalcula el total de una cotización sumando subtotales
-- ============================================================
CREATE OR REPLACE FUNCTION fn_recalcular_total_cotizacion(p_cotizacion_id UUID)
RETURNS DECIMAL
LANGUAGE plpgsql
AS $$
DECLARE
    v_total DECIMAL(14,2);
BEGIN
    SELECT COALESCE(SUM(subtotal), 0)
    INTO v_total
    FROM cotizacion_item
    WHERE cotizacion_id = p_cotizacion_id;

    UPDATE cotizacion
    SET total = v_total,
        updated_at = NOW()
    WHERE id = p_cotizacion_id;

    RETURN v_total;
END;
$$;

COMMENT ON FUNCTION fn_recalcular_total_cotizacion(UUID) IS 'Recalcula el total de una cotización a partir de sus ítems';

-- ============================================================
-- Función: fn_crear_evento_desde_cotizacion
-- Descripción: Crea un evento a partir de una cotización aprobada
-- ============================================================
CREATE OR REPLACE FUNCTION fn_crear_evento_desde_cotizacion(p_cotizacion_id UUID)
RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
    v_cotizacion RECORD;
    v_evento_id UUID;
BEGIN
    -- Verificar que la cotización esté aprobada
    SELECT id, codigo, estado
    INTO v_cotizacion
    FROM cotizacion
    WHERE id = p_cotizacion_id;

    IF v_cotizacion.estado != 'aprobada' THEN
        RAISE EXCEPTION 'La cotización debe estar aprobada para crear un evento. Estado actual: %', v_cotizacion.estado;
    END IF;

    -- Crear el evento
    INSERT INTO evento (cotizacion_id, nombre)
    VALUES (p_cotizacion_id, 'Evento - ' || v_cotizacion.codigo)
    RETURNING id INTO v_evento_id;

    RETURN v_evento_id;
END;
$$;

COMMENT ON FUNCTION fn_crear_evento_desde_cotizacion(UUID) IS 'Crea un evento nuevo a partir de una cotización aprobada';

-- ============================================================
-- Función: fn_calcular_valor_turno
-- Descripción: Calcula el valor del turno desde el portafolio del proveedor
-- ============================================================
CREATE OR REPLACE FUNCTION fn_calcular_valor_turno(p_evento_personal_id UUID)
RETURNS DECIMAL
LANGUAGE plpgsql
AS $$
DECLARE
    v_personal RECORD;
    v_precio DECIMAL(12,2);
BEGIN
    -- Obtener datos del personal del evento
    SELECT proveedor_id, servicio_id
    INTO v_personal
    FROM evento_personal
    WHERE id = p_evento_personal_id;

    -- Buscar precio en portafolio
    SELECT precio_unitario
    INTO v_precio
    FROM portafolio
    WHERE proveedor_id = v_personal.proveedor_id
      AND servicio_id = v_personal.servicio_id
      AND activo = TRUE;

    -- Si no se encuentra, dejar en 0
    IF v_precio IS NULL THEN
        v_precio := 0;
    END IF;

    -- Actualizar valor del turno
    UPDATE evento_personal
    SET valor_turno = v_precio
    WHERE id = p_evento_personal_id;

    RETURN v_precio;
END;
$$;

COMMENT ON FUNCTION fn_calcular_valor_turno(UUID) IS 'Calcula el valor del turno consultando el portafolio del proveedor';

-- ============================================================
-- Función: fn_estadisticas_leads
-- Descripción: Retorna estadísticas de leads agrupados por estado
-- ============================================================
CREATE OR REPLACE FUNCTION fn_estadisticas_leads()
RETURNS TABLE(estado VARCHAR, cantidad BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT l.estado::VARCHAR, COUNT(*)::BIGINT AS cantidad
    FROM lead l
    WHERE l.activo = TRUE
    GROUP BY l.estado
    ORDER BY cantidad DESC;
END;
$$;

COMMENT ON FUNCTION fn_estadisticas_leads() IS 'Retorna conteo de leads agrupados por estado';

-- ============================================================
-- Función: fn_cotizaciones_por_vencer
-- Descripción: Retorna cotizaciones próximas a vencer en N días
-- ============================================================
CREATE OR REPLACE FUNCTION fn_cotizaciones_por_vencer(p_dias INTEGER DEFAULT 7)
RETURNS SETOF cotizacion
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT c.*
    FROM cotizacion c
    WHERE c.fecha_vencimiento BETWEEN CURRENT_DATE AND CURRENT_DATE + p_dias
      AND c.estado NOT IN ('aprobada', 'rechazada', 'vencida')
      AND c.activo = TRUE
    ORDER BY c.fecha_vencimiento ASC;
END;
$$;

COMMENT ON FUNCTION fn_cotizaciones_por_vencer(INTEGER) IS 'Retorna cotizaciones que vencen dentro de los próximos N días';

-- ============================================================
-- Trigger Function: trg_fn_actualizar_stock
-- Descripción: Actualiza stock_actual del insumo al registrar movimiento
-- ============================================================
CREATE OR REPLACE FUNCTION trg_fn_actualizar_stock()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_stock_actual DECIMAL(10,2);
BEGIN
    -- Obtener stock actual del insumo
    SELECT stock_actual
    INTO v_stock_actual
    FROM insumo
    WHERE id = NEW.insumo_id;

    IF NEW.tipo_movimiento = 'ingreso' THEN
        -- Sumar al stock
        UPDATE insumo
        SET stock_actual = stock_actual + NEW.cantidad,
            updated_at = NOW()
        WHERE id = NEW.insumo_id;

    ELSIF NEW.tipo_movimiento = 'retiro' THEN
        -- Verificar que hay suficiente stock
        IF v_stock_actual - NEW.cantidad < 0 THEN
            RAISE EXCEPTION 'No hay suficiente stock para este retiro. Stock actual: %, cantidad solicitada: %', v_stock_actual, NEW.cantidad;
        END IF;

        -- Restar del stock
        UPDATE insumo
        SET stock_actual = stock_actual - NEW.cantidad,
            updated_at = NOW()
        WHERE id = NEW.insumo_id;
    END IF;

    RETURN NEW;
END;
$$;

-- Crear trigger en insumo_movimiento
CREATE TRIGGER trg_actualizar_stock
    AFTER INSERT ON insumo_movimiento
    FOR EACH ROW
    EXECUTE FUNCTION trg_fn_actualizar_stock();

COMMENT ON FUNCTION trg_fn_actualizar_stock() IS 'Trigger que actualiza el stock del insumo al registrar un movimiento';

-- ============================================================
-- Trigger Function: trg_fn_calcular_subtotal_item
-- Descripción: Calcula el subtotal de un ítem aplicando descuento/recargo
-- ============================================================
CREATE OR REPLACE FUNCTION trg_fn_calcular_subtotal_item()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_subtotal DECIMAL(14,2);
    v_tipo_nombre VARCHAR(50);
    v_porcentaje DECIMAL(5,2);
BEGIN
    -- Calcular subtotal base
    v_subtotal := NEW.cantidad * NEW.precio_unitario;

    -- Aplicar descuento o recargo si existe
    IF NEW.descuento_recargo_id IS NOT NULL THEN
        SELECT dr.valor, tdr.nombre
        INTO v_porcentaje, v_tipo_nombre
        FROM descuento_recargo dr
        JOIN tipo_descuento_recargo tdr ON tdr.id = dr.tipo_id
        WHERE dr.id = NEW.descuento_recargo_id;

        IF v_tipo_nombre = 'Descuento' THEN
            v_subtotal := v_subtotal - (v_subtotal * v_porcentaje / 100);
        ELSIF v_tipo_nombre = 'Recargo' THEN
            v_subtotal := v_subtotal + (v_subtotal * v_porcentaje / 100);
        END IF;
    END IF;

    NEW.subtotal := v_subtotal;
    RETURN NEW;
END;
$$;

-- Crear trigger en cotizacion_item
CREATE TRIGGER trg_calcular_subtotal_item
    BEFORE INSERT OR UPDATE ON cotizacion_item
    FOR EACH ROW
    EXECUTE FUNCTION trg_fn_calcular_subtotal_item();

COMMENT ON FUNCTION trg_fn_calcular_subtotal_item() IS 'Trigger que calcula el subtotal del ítem aplicando descuentos/recargos';

-- ============================================================
-- Trigger Function: trg_fn_recalcular_total_cotizacion
-- Descripción: Recalcula el total de la cotización al modificar ítems
-- ============================================================
CREATE OR REPLACE FUNCTION trg_fn_recalcular_total_cotizacion()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_cotizacion_id UUID;
BEGIN
    -- Determinar la cotización afectada
    IF TG_OP = 'DELETE' THEN
        v_cotizacion_id := OLD.cotizacion_id;
    ELSE
        v_cotizacion_id := NEW.cotizacion_id;
    END IF;

    -- Recalcular total
    PERFORM fn_recalcular_total_cotizacion(v_cotizacion_id);

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$;

-- Crear trigger en cotizacion_item
CREATE TRIGGER trg_recalcular_total
    AFTER INSERT OR UPDATE OR DELETE ON cotizacion_item
    FOR EACH ROW
    EXECUTE FUNCTION trg_fn_recalcular_total_cotizacion();

COMMENT ON FUNCTION trg_fn_recalcular_total_cotizacion() IS 'Trigger que recalcula el total de la cotización al modificar ítems';
