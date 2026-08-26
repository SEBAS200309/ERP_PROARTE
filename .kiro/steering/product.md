# Product Document: ERP Pro Arte

## 1. Visión y Propósito del Producto

**ERP Pro Arte** es un sistema integral de planificación de recursos empresariales diseñado para la empresa colombiana **Eventos Pro Arte**, dedicada a la producción técnica, logística y artística de eventos, conciertos y espectáculos corporativos.

El objetivo principal del sistema es centralizar, estandarizar y automatizar el ciclo operativo completo de la compañía:
*   **Gestión Comercial:** Captura y seguimiento de oportunidades (leads), directorio de clientes/empresas, catálogo de servicios propios y tercerizados, y estructuración de cotizaciones comerciales con cálculo dinámico de costos, recargos y descuentos.
*   **Gestión Operativa de Eventos:** Conversión de cotizaciones aprobadas en eventos ejecutables, asignación de personal técnico y logístico con verificación de cumplimiento normativo (seguridad social ARL y órdenes de prestación OP), coordinación de proveedores tercerizados y control de minutas/observaciones.
*   **Logística e Inventario:** Control de ingresos, retiros y existencias de insumos técnicos y consumibles, además de la planificación y control de alimentación para el personal de cada evento.
*   **Compras y Documentación:** Emisión de órdenes de compra para proveedores, exportación masiva a Excel y generación automática de documentos comerciales y técnicos en formato PDF.

---

## 2. Usuarios y Roles del Sistema

El sistema implementa un modelo de control de acceso basado en roles (RBAC) con permisos granulares por tabla y contexto relacional en formato JSON:

| Rol | Descripción | Responsabilidades Principales |
| :--- | :--- | :--- |
| **Administrador (`ADMIN`)** | Control total de la plataforma | Gestión de usuarios, asignación y edición de permisos JSON, mantenimiento de catálogos del sistema, configuración global. |
| **Comercial (`COMERCIAL`)** | Gestión de ventas y clientes | Registro y seguimiento de leads, administración de clientes/empresas, creación y envío de cotizaciones, generación de presentaciones y PDFs. |
| **Operativo (`OPERATIVO`)** | Ejecución técnica y logística | Transformación de cotizaciones aprobadas a eventos, contratación y asignación de personal, control de ARL/OP, solicitudes a proveedores y órdenes de compra, control de inventario y alimentación. |
| **Coordinador (`COORDINADOR`)** | Supervisión en campo | Consulta de eventos asignados, registro de observaciones en tiempo real, seguimiento de turnos del personal en sitio. |

---

## 3. Ciclo Operativo del Negocio (Flujo End-to-End)

El flujo de valor del ERP Pro Arte conecta las áreas comercial, operativa y administrativa a través de las siguientes etapas:

```
[1. Lead Comercial] ───> [2. Persona / Empresa] ───> [3. Cotización]
                                                            │
                                                     (Aprobación Cliente)
                                                            │
                                                            ▼
[6. Compras y Cierre] <─── [5. Logística / Personal] <─── [4. Evento Operativo]
  - Órdenes de Compra        - Asignación de Turnos        - Coordinación Proveedores
  - Descarga Excel / PDF     - Validación ARL / OP         - Insumos & Alimentación
```

### Paso a Paso del Proceso:
1.  **Captura de la Oportunidad (Lead):** Un cliente solicita un servicio para un evento. Se crea un registro de lead con su estado inicial (`nuevo`, `contactado`, `en_negociacion`).
2.  **Registro de Entidades:** Se vincula o crea la `Persona` (contacto) y la `Empresa` asociada, asignando tipos de documento y roles de entidad normalizados.
3.  **Elaboración de la Cotización:** El área comercial añade servicios del catálogo (sonido, luces, tarimas, artistas), define cantidades, precios unitarios y aplica descuentos o recargos porcentuales. El sistema calcula automáticamente subtotales y total general.
4.  **Aprobación y Creación del Evento:** Una vez el cliente aprueba la cotización (estado `aprobada`), el sistema habilita la creación formal del `Evento` operativo.
5.  **Planificación Operativa y Personal:**
    *   Se asocian proveedores externos y sus servicios portafolio.
    *   Se contrata y programa el personal técnico para el evento (asignación de turnos con cálculo de valor).
    *   **Control de Riesgo Laboral:** El sistema valida visualmente si cada trabajador cuenta con ARL y contrato OP vigente antes de ingresar al evento.
    *   **Insumos y Catering:** Se registran salidas de inventario asignadas al evento y se gestiona la recepción/distribución de alimentación.
6.  **Formalización y Compras:** Se generan las Órdenes de Compra a proveedores derivadas de las solicitudes de servicio, permitiendo la descarga masiva en hojas de cálculo Excel y reportes PDF.

---

## 4. Módulos Funcionales del Producto

1.  **Autenticación y Seguridad:** Inicio de sesión seguro con JWT, validación de sesiones, expiración de tokens (8 horas de acceso, 7 días de refresco) y cierre de sesión.
2.  **Usuarios y Permisos:** Panel administrativo para crear usuarios y editar visualmente las matrices JSON de permisos (ver listado, ver detalle, crear, editar, eliminar y contexto visible).
3.  **Contactos y Clientes (Personas y Empresas):** Directorio centralizado con búsqueda predictiva, clasificación por rol (cliente, contacto, proveedor) y relaciones muchos a muchos.
4.  **Catálogo de Servicios y Costos:** Jerarquía de servicios y subservicios, categorías (propios vs. tercerizados), y reglas de porcentajes de descuento/recargo.
5.  **Proveedores y Portafolio:** Gestión de aliados comerciales, servicios que prestan con precios pactados y solicitudes formales de contratación.
6.  **Cotizaciones Comerciales:** Generador de propuestas, control de fechas de vencimiento, alertas de cotizaciones próximas a expirar y exportación a PDF formal.
7.  **Eventos:** Centro de comando operativo por evento: fechas, ubicaciones, minutas de observación, personal asignado y balance logístico.
8.  **Personal y Turnos:** Control de turnos de trabajadores, cálculo de tarifas por rol/proveedor y semáforo de cumplimiento ARL/OP.
9.  **Inventario de Insumos:** Kardex de movimientos (ingresos/retiros), cálculo automático de existencias y bloqueo de retiros sin stock suficiente.
10. **Alimentación de Eventos:** Planificación de refrigerios y comidas para staff técnico durante montajes y presentaciones.
11. **Órdenes de Compra:** Control del ciclo de compras operativas y exportación de planillas para tesorería/contabilidad.
12. **Catálogos del Sistema:** Mantenimiento de tablas maestras normalizadas (tipos de documento, roles de entidad, estados por contexto, unidades de medida, categorías de servicio, roles de evento).

---

## 5. Reglas de Negocio Clave

*   **Eliminación Lógica Universal (Soft Delete):** Ningún registro de negocio se elimina físicamente de la base de datos; se marca como inactivo (`activo = false`) para preservar la trazabilidad histórica y la integridad referencial.
*   **Inmutabilidad de Autoría:** Todo registro almacena el identificador del usuario creador (`created_by`) y marcas temporales de auditoría (`created_at`, `updated_at`).
*   **Compuerta de Aprobación de Eventos:** No es posible crear un evento si su cotización origen no se encuentra en estado formal `aprobada`.
*   **Integridad de Stock:** No se permiten retiros de inventario si la cantidad solicitada supera las existencias actuales (`stock_actual`).
*   **Normalización Estricta:** Ningún catálogo utiliza cadenas de texto libre en campos estructurados; todo valor maestro se enlaza mediante identificadores universales (UUID) a su respectiva tabla lookup.
