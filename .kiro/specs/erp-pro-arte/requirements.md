# Requirements Document

## Introduction

Sistema ERP web para la empresa Pro Arte que permite gestionar el ciclo completo de operaciones: usuarios, clientes, proveedores, catalogo de servicios, cotizaciones, eventos, ordenes de compra, comunicaciones, presentaciones, personal, inventario y alimentacion de eventos.

Arquitectura: Frontend Angular + Backend API REST Spring Boot + PostgreSQL (logica de negocio en DB mediante functions, triggers y procedures).

Nota: Se espera recibir diagrama ERD y UML de clases para proceder al Design.

## Glossary

- **ERP**: Enterprise Resource Planning - Sistema de planificacion de recursos empresariales
- **Lead**: Oportunidad comercial o solicitud de servicio por parte de un potencial cliente
- **Cotizacion**: Propuesta comercial con detalle de servicios y costos enviada a un cliente
- **Evento**: Actividad o servicio a ejecutar derivado de una cotizacion aprobada
- **Proveedor**: Persona o empresa que ofrece servicios tercerizados a Pro Arte
- **Portafolio**: Conjunto de servicios que un proveedor ofrece
- **Orden de Compra**: Documento que formaliza la solicitud de servicios a un proveedor
- **Procedure/Function**: Logica de negocio implementada directamente en PostgreSQL
- **CRUD**: Operaciones de Crear, Leer, Actualizar y Eliminar sobre registros
- **ARL**: Administradora de Riesgos Laborales
- **OP**: Orden de Prestacion de servicios

## Requirements

### Requirement 1: Gestion de usuarios y control de acceso

**User Story:** Como administrador, quiero gestionar los usuarios del sistema y sus permisos basados en el modelo de permisos de PostgreSQL (en formato JSON), para controlar quien tiene acceso a que tablas y acciones puede realizar, todo gestionable desde la interfaz web.

#### Acceptance Criteria

1. CUANDO el administrador accede al modulo de usuarios ENTONCES el sistema DEBE mostrar la lista de usuarios registrados con sus roles y estado
2. CUANDO el administrador crea un nuevo usuario ENTONCES el sistema DEBE registrar el usuario con sus credenciales y rol asignado
3. CUANDO el administrador actualiza un usuario ENTONCES el sistema DEBE reflejar los cambios inmediatamente
4. CUANDO el administrador elimina un usuario ENTONCES el sistema DEBE desactivar el acceso del usuario (eliminacion logica)
5. CUANDO un usuario intenta iniciar sesion con credenciales validas ENTONCES el sistema DEBE permitir el acceso y redirigir al dashboard
6. CUANDO un usuario intenta iniciar sesion con credenciales invalidas ENTONCES el sistema DEBE mostrar "Credenciales incorrectas. Verifique su usuario y contrasena"
7. CUANDO el sistema registra una accion de creacion ENTONCES DEBE asociar el usuario a la creacion de cotizaciones, personas, empresas y proveedores
8. CUANDO el administrador consulta permisos de un usuario ENTONCES el sistema DEBE mostrar la configuracion JSON de permisos que define: tablas visibles, acciones permitidas (ver, crear, editar, eliminar) y contexto visible (tablas relacionadas)
9. CUANDO el administrador modifica permisos ENTONCES el sistema DEBE invocar un endpoint que actualice el JSON de configuracion de permisos del rol en la tabla permiso de PostgreSQL
10. SI un usuario no tiene sesion activa ENTONCES el sistema DEBE redirigir al login
11. SI el usuario intenta acceder a un modulo sin permisos ENTONCES el sistema DEBE mostrar "No tiene permisos para realizar esta accion"
12. CUANDO el usuario visualiza un registro individual ENTONCES el sistema DEBE mostrar la vista detalle con toda la informacion del registro y el contexto (tablas relacionadas por llave foranea) segun los permisos del usuario
13. CUANDO el usuario no tiene permiso para ver una tabla relacionada ENTONCES el sistema NO DEBE mostrar esa tabla en el contexto del registro
14. CUANDO el administrador asigna permisos ENTONCES el sistema DEBE permitir configurar por cada tabla: ver listado, ver detalle, crear, editar y eliminar de forma granular
15. CUANDO se guardan los permisos de un usuario ENTONCES el sistema DEBE almacenarlos en formato JSON en la tabla de permisos y el backend DEBE validar contra ese JSON en cada request

### Requirement 2: Gestion de clientes, empresas y contactos comerciales

**User Story:** Como comercial, quiero gestionar leads, personas y empresas con sus relaciones, para apoyar el proceso de gestion comercial de la organizacion.

#### Acceptance Criteria

1. CUANDO el usuario registra un lead ENTONCES el sistema DEBE almacenar la solicitud con su estado inicial
2. CUANDO el usuario actualiza un lead ENTONCES el sistema DEBE reflejar los cambios en la lista
3. CUANDO el usuario elimina un lead ENTONCES el sistema DEBE removerlo de la vista activa
4. CUANDO el usuario consulta leads ENTONCES el sistema DEBE mostrar la lista con filtros y paginacion
5. CUANDO el usuario solicita graficos de leads ENTONCES el sistema DEBE generar graficos de pastel con la distribucion por estado
6. CUANDO el usuario registra una persona ENTONCES el sistema DEBE almacenar sus datos y asociar al usuario creador
7. CUANDO el usuario actualiza una persona ENTONCES el sistema DEBE reflejar los cambios
8. CUANDO el usuario elimina una persona ENTONCES el sistema DEBE realizar eliminacion logica
9. CUANDO el usuario consulta personas ENTONCES el sistema DEBE mostrar la lista con busqueda y filtros
10. CUANDO el usuario registra una empresa ENTONCES el sistema DEBE almacenar los datos y asociar al usuario creador
11. CUANDO el usuario actualiza una empresa ENTONCES el sistema DEBE reflejar los cambios
12. CUANDO el usuario elimina una empresa ENTONCES el sistema DEBE realizar eliminacion logica
13. CUANDO el usuario consulta empresas ENTONCES el sistema DEBE mostrar la lista con busqueda y filtros
14. CUANDO el usuario asocia una persona a una empresa ENTONCES el sistema DEBE crear la relacion persona-empresa
15. CUANDO el usuario asigna un rol (contacto, cliente o proveedor) a una persona ENTONCES el sistema DEBE actualizar el rol en la base de datos
16. CUANDO el usuario asigna un rol (cliente o proveedor) a una empresa ENTONCES el sistema DEBE actualizar el rol en la base de datos

### Requirement 3: Gestion de proveedores y servicios tercerizados

**User Story:** Como operativo, quiero gestionar proveedores, sus portafolios de servicios y solicitudes de contratacion, para controlar los servicios tercerizados disponibles para eventos.

#### Acceptance Criteria

1. CUANDO el usuario registra un proveedor ENTONCES el sistema DEBE almacenar sus datos completos
2. CUANDO el usuario actualiza un proveedor ENTONCES el sistema DEBE reflejar los cambios
3. CUANDO el usuario elimina un proveedor ENTONCES el sistema DEBE realizar eliminacion logica
4. CUANDO el usuario consulta proveedores ENTONCES el sistema DEBE mostrar la lista con filtros
5. CUANDO el usuario registra un portafolio ENTONCES el sistema DEBE asociar uno o mas servicios al proveedor
6. CUANDO el usuario actualiza un portafolio ENTONCES el sistema DEBE reflejar los cambios en servicios asociados
7. CUANDO el usuario elimina un portafolio ENTONCES el sistema DEBE desasociar los servicios del proveedor
8. CUANDO el usuario consulta portafolios ENTONCES el sistema DEBE mostrar los servicios agrupados por proveedor
9. CUANDO el usuario registra una solicitud de servicio ENTONCES el sistema DEBE asociarla al proveedor correspondiente
10. CUANDO el usuario actualiza una solicitud ENTONCES el sistema DEBE reflejar los cambios de estado
11. CUANDO el usuario elimina una solicitud ENTONCES el sistema DEBE realizar eliminacion logica
12. CUANDO el usuario consulta solicitudes ENTONCES el sistema DEBE mostrar la lista con estado actual

### Requirement 4: Gestion del catalogo de servicios y estructura de costos

**User Story:** Como comercial, quiero gestionar servicios, subservicios y porcentajes de adicion o descuento, para configurar la oferta comercial y estructura de precios de la organizacion.

#### Acceptance Criteria

1. CUANDO el usuario registra un servicio ENTONCES el sistema DEBE almacenarlo con su categoria (propio o de tercero)
2. CUANDO el usuario actualiza un servicio ENTONCES el sistema DEBE reflejar los cambios
3. CUANDO el usuario elimina un servicio ENTONCES el sistema DEBE realizar eliminacion logica
4. CUANDO el usuario consulta servicios ENTONCES el sistema DEBE mostrar la lista con filtros por categoria
5. CUANDO el usuario categoriza un servicio ENTONCES el sistema DEBE distinguir si es propio de la organizacion o de terceros
6. CUANDO el usuario asigna un servicio como subservicio de otro ENTONCES el sistema DEBE crear la relacion jerarquica
7. CUANDO el usuario marca un servicio ENTONCES el sistema DEBE permitir identificar si necesita orden de compra
8. CUANDO el usuario registra un porcentaje de adicion o descuento ENTONCES el sistema DEBE almacenarlo
9. CUANDO el usuario actualiza un porcentaje ENTONCES el sistema DEBE reflejar los cambios
10. CUANDO el usuario elimina un porcentaje ENTONCES el sistema DEBE realizar eliminacion logica
11. CUANDO el usuario consulta porcentajes ENTONCES el sistema DEBE mostrar la lista activa
12. CUANDO el usuario aplica un porcentaje a una cotizacion ENTONCES el sistema DEBE recalcular el total via function de PostgreSQL
13. CUANDO el usuario aplica un porcentaje a un servicio o sub-servicio ENTONCES el sistema DEBE recalcular el costo
14. CUANDO se modifican sub-servicios, descuentos o recargos ENTONCES el sistema DEBE calcular automaticamente el costo total del servicio via function o trigger de PostgreSQL

### Requirement 5: Gestion integral de cotizaciones

**User Story:** Como comercial, quiero gestionar el ciclo completo de cotizaciones incluyendo creacion, seguimiento, vigencia y estados, para controlar las propuestas comerciales enviadas a clientes.

#### Acceptance Criteria

1. CUANDO el usuario registra una cotizacion ENTONCES el sistema DEBE almacenarla con estado inicial y asociar al usuario creador
2. CUANDO el usuario actualiza una cotizacion ENTONCES el sistema DEBE reflejar los cambios y mantener historial
3. CUANDO el usuario consulta cotizaciones ENTONCES el sistema DEBE mostrar la lista con filtros por estado, cliente y fecha
4. CUANDO el usuario elimina una cotizacion ENTONCES el sistema DEBE realizar eliminacion logica
5. CUANDO el usuario asigna fecha de vencimiento ENTONCES el sistema DEBE registrarla para control de vigencia
6. CUANDO una cotizacion esta proxima a vencerse ENTONCES el sistema DEBE notificar al usuario via function o trigger de PostgreSQL
7. CUANDO el usuario consulta cotizaciones proximas a vencer ENTONCES el sistema DEBE mostrar un listado filtrado
8. CUANDO el usuario aplica porcentajes de adicion o descuento ENTONCES el sistema DEBE recalcular el total
9. CUANDO el usuario asocia clientes o contactos a una cotizacion ENTONCES el sistema DEBE registrar la relacion
10. CUANDO el usuario cambia el estado de una cotizacion ENTONCES el sistema DEBE actualizar el estado y registrar la transicion

### Requirement 6: Gestion de eventos derivados de cotizaciones aprobadas

**User Story:** Como operativo, quiero gestionar eventos generados a partir de cotizaciones aprobadas, para coordinar servicios, proveedores, personal y recursos necesarios para su ejecucion.

#### Acceptance Criteria

1. CUANDO una cotizacion es aprobada ENTONCES el sistema DEBE permitir crear un evento a partir de ella via procedure de PostgreSQL
2. CUANDO el usuario asocia proveedores a un evento ENTONCES el sistema DEBE registrar la relacion evento-proveedor
3. CUANDO el usuario asocia servicios a un evento ENTONCES el sistema DEBE registrar los servicios segun la cotizacion
4. CUANDO el usuario asocia personas como promotores ENTONCES el sistema DEBE registrar el rol promotor en el evento
5. CUANDO el usuario asocia personas como contactos ENTONCES el sistema DEBE registrar el rol contacto en el evento
6. CUANDO el usuario asocia personas como coordinadores ENTONCES el sistema DEBE registrar el rol coordinador en el evento
7. CUANDO el usuario asocia personas como personal de trabajo ENTONCES el sistema DEBE registrar el rol personal en el evento
8. CUANDO el usuario asocia alimentacion a un evento ENTONCES el sistema DEBE registrar la planificacion alimentaria
9. CUANDO el usuario asocia insumos a un evento ENTONCES el sistema DEBE registrar los insumos requeridos
10. CUANDO el usuario registra observaciones de un evento ENTONCES el sistema DEBE almacenarlas
11. CUANDO el usuario actualiza observaciones de un evento ENTONCES el sistema DEBE reflejar los cambios
12. CUANDO el usuario consulta observaciones de un evento ENTONCES el sistema DEBE mostrar el listado

### Requirement 7: Gestion de ordenes de compra

**User Story:** Como operativo, quiero gestionar ordenes de compra basadas en solicitudes de servicios a proveedores, para controlar las adquisiciones necesarias para eventos y operaciones.

#### Acceptance Criteria

1. CUANDO el usuario registra una orden de compra ENTONCES el sistema DEBE asociarla a la solicitud de servicio correspondiente
2. CUANDO el usuario actualiza una orden de compra ENTONCES el sistema DEBE reflejar los cambios
3. CUANDO el usuario elimina una orden de compra ENTONCES el sistema DEBE realizar eliminacion logica
4. CUANDO el usuario consulta ordenes de compra ENTONCES el sistema DEBE mostrar la lista con filtros
5. CUANDO el usuario solicita descarga masiva ENTONCES el sistema DEBE generar un archivo Excel con las ordenes seleccionadas via procedure de PostgreSQL y endpoint de descarga

### Requirement 8: Gestion de comunicaciones y generacion de documentos

**User Story:** Como comercial, quiero gestionar plantillas de mensajes y generar documentos Excel y PDF, para estandarizar las comunicaciones y documentacion comercial.

#### Acceptance Criteria

1. CUANDO el usuario solicita descargar plantilla de proveedores de un evento ENTONCES el sistema DEBE generar un Excel con la informacion
2. CUANDO el usuario solicita descarga masiva de ordenes de compra ENTONCES el sistema DEBE generar un Excel
3. CUANDO el usuario registra un mensaje personalizado ENTONCES el sistema DEBE almacenar la plantilla
4. CUANDO el usuario actualiza un mensaje personalizado ENTONCES el sistema DEBE reflejar los cambios
5. CUANDO el usuario elimina un mensaje personalizado ENTONCES el sistema DEBE realizar eliminacion logica
6. CUANDO el usuario consulta mensajes personalizados ENTONCES el sistema DEBE mostrar la lista de plantillas
7. CUANDO el usuario solicita generar PDF de presentacion ENTONCES el sistema DEBE generar y descargar el documento
8. CUANDO el usuario solicita descargar cotizacion en PDF ENTONCES el sistema DEBE generar el documento con formato

### Requirement 9: Gestion integral de presentaciones

**User Story:** Como comercial, quiero gestionar presentaciones de servicios y productos, para tener material comercial actualizado disponible.

#### Acceptance Criteria

1. CUANDO el usuario registra una presentacion ENTONCES el sistema DEBE almacenar la informacion del servicio o producto presentado
2. CUANDO el usuario actualiza una presentacion ENTONCES el sistema DEBE reflejar los cambios
3. CUANDO el usuario elimina una presentacion ENTONCES el sistema DEBE realizar eliminacion logica
4. CUANDO el usuario consulta presentaciones ENTONCES el sistema DEBE mostrar la lista con filtros

### Requirement 10: Gestion integral de personal de un evento

**User Story:** Como operativo, quiero gestionar el personal contratado para un evento incluyendo turnos, servicios asignados y documentacion, para coordinar la contratacion y asignacion de trabajadores.

#### Acceptance Criteria

1. CUANDO el usuario registra personal para un evento ENTONCES el sistema DEBE almacenar la asignacion
2. CUANDO el usuario actualiza personal de un evento ENTONCES el sistema DEBE reflejar los cambios
3. CUANDO el usuario elimina personal de un evento ENTONCES el sistema DEBE desasociar al empleado
4. CUANDO el usuario consulta personal de un evento ENTONCES el sistema DEBE mostrar la lista con detalles
5. CUANDO el usuario registra observaciones individuales del personal ENTONCES el sistema DEBE almacenarlas por empleado
6. CUANDO se asigna un turno a un empleado ENTONCES el sistema DEBE calcular automaticamente el valor del turno via function de PostgreSQL
7. CUANDO el usuario asocia servicios del portafolio de un proveedor a su contratacion ENTONCES el sistema DEBE registrar que servicio prestara como empleado del evento
8. CUANDO el usuario consulta ARL de un empleado ENTONCES el sistema DEBE mostrar si cuenta con ARL vigente
9. CUANDO el usuario consulta OP de un empleado ENTONCES el sistema DEBE mostrar si cuenta con OP vigente
10. SI un empleado no tiene ARL vigente ENTONCES el sistema DEBE mostrar advertencia visual
11. SI un empleado no tiene OP vigente ENTONCES el sistema DEBE mostrar advertencia visual

### Requirement 11: Gestion integral de inventario de insumos

**User Story:** Como operativo, quiero gestionar los ingresos y retiros del inventario de insumos, para controlar existencias y movimientos de la organizacion.

#### Acceptance Criteria

1. CUANDO el usuario registra un ingreso al inventario ENTONCES el sistema DEBE almacenar el movimiento con fecha, cantidad y detalle
2. CUANDO el usuario consulta ingresos del inventario ENTONCES el sistema DEBE mostrar el historial de ingresos
3. CUANDO el usuario registra un retiro del inventario ENTONCES el sistema DEBE almacenar el movimiento con fecha, cantidad y motivo
4. CUANDO el usuario consulta retiros del inventario ENTONCES el sistema DEBE mostrar el historial de retiros
5. CUANDO el usuario consulta el inventario ENTONCES el sistema DEBE mostrar el estado actual de existencias
6. CUANDO se registra un ingreso o retiro ENTONCES el sistema DEBE calcular automaticamente la cantidad actual via trigger de PostgreSQL
7. SI se intenta retirar mas de lo disponible ENTONCES el sistema DEBE mostrar "No hay suficiente stock para este retiro"
8. SI el inventario de un insumo llega a cero ENTONCES el sistema DEBE mostrar indicador visual de agotado

### Requirement 12: Gestion de alimentacion de un evento

**User Story:** Como operativo, quiero gestionar la alimentacion planificada para un evento, para controlar los insumos alimenticios necesarios.

#### Acceptance Criteria

1. CUANDO el usuario registra un ingreso de alimentacion ENTONCES el sistema DEBE almacenar el movimiento
2. CUANDO el usuario consulta ingresos de alimentacion ENTONCES el sistema DEBE mostrar el historial
3. CUANDO el usuario registra un retiro de alimentacion ENTONCES el sistema DEBE almacenar el movimiento
4. CUANDO el usuario consulta retiros de alimentacion ENTONCES el sistema DEBE mostrar el historial
5. CUANDO el usuario consulta alimentacion del evento ENTONCES el sistema DEBE mostrar el estado actual
6. CUANDO se registra un ingreso o retiro ENTONCES el sistema DEBE calcular automaticamente las cantidades disponibles via trigger de PostgreSQL
7. SI se intenta retirar mas de lo disponible ENTONCES el sistema DEBE mostrar "No hay suficiente cantidad para este retiro"
