# Checklist de Pruebas de Integración End-to-End — ERP Pro Arte

**Total estimado de casos de prueba:** ~180

**Fecha de creación:** 2025  
**Última actualización:** Pendiente  
**Responsable de ejecución:** QA / Desarrollo  
**Estado:** Pendiente de ejecución

---

## 1. Prerequisitos

Antes de ejecutar las pruebas, verificar que el entorno esté correctamente configurado:

- [ ] Docker ejecutándose con contenedor PostgreSQL activo
- [ ] Base de datos con migraciones Flyway aplicadas (V1 a V10)
- [ ] Backend Spring Boot corriendo en `http://localhost:8080`
- [ ] Frontend Angular corriendo en `http://localhost:4200`
- [ ] Datos semilla cargados (usuario admin, catálogos base, roles)
- [ ] Credenciales de admin por defecto disponibles: `admin / [contraseña configurada]`
- [ ] Al menos un usuario con rol "Comercial" creado para pruebas de permisos
- [ ] Navegador con DevTools disponible para inspeccionar respuestas HTTP

---

## 2. Flujo de Autenticación (Requirement 1)

### 2.1 Login exitoso
- [ ] Ingresar credenciales válidas → redirige a `/dashboard`
- [ ] El token JWT se almacena correctamente (localStorage o cookie httpOnly)
- [ ] El nombre del usuario se muestra en la interfaz después del login

### 2.2 Login fallido
- [ ] Ingresar credenciales inválidas → muestra "Credenciales incorrectas. Verifique su usuario y contraseña"
- [ ] El mensaje de error se muestra en español
- [ ] No se almacena ningún token tras intento fallido

### 2.3 Protección de rutas
- [ ] Acceder a ruta protegida sin sesión → redirige a `/auth/login`
- [ ] Acceder a `/dashboard` directamente sin token → redirige a login
- [ ] Acceder a `/api/v1/usuarios` sin Authorization header → responde 401

### 2.4 Expiración y renovación de sesión
- [ ] Cuando el JWT expira → redirige automáticamente al login
- [ ] El mensaje "Su sesión ha expirado. Inicie sesión nuevamente" se muestra
- [ ] Refresh token extiende la sesión correctamente (si está implementado)

### 2.5 Logout
- [ ] Cerrar sesión → elimina token y redirige a login
- [ ] Tras logout, intentar navegar atrás no permite acceso

---

## 3. Validación CRUD por Módulo

Para cada módulo, verificar las 7 operaciones base. Marcar cada celda al completar.

### 3.1 Usuarios (`/api/v1/usuarios`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona (avanzar/retroceder páginas)
- [ ] Búsqueda/filtro funciona correctamente
- [ ] Crear usuario → aparece en la lista
- [ ] Editar usuario → cambios reflejados
- [ ] Eliminar usuario → desaparece de la lista (eliminación lógica)
- [ ] Ver detalle → muestra registro completo con contexto

### 3.2 Leads (`/api/v1/leads`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Búsqueda/filtro funciona
- [ ] Crear lead → aparece en la lista con estado inicial
- [ ] Editar lead → cambios reflejados
- [ ] Eliminar lead → desaparece de la lista
- [ ] Ver detalle → muestra registro con contexto
- [ ] Gráfico de distribución por estado se genera correctamente

### 3.3 Personas (`/api/v1/personas`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Búsqueda/filtro funciona
- [ ] Crear persona → aparece en la lista (asociada al usuario creador)
- [ ] Editar persona → cambios reflejados
- [ ] Eliminar persona → desaparece de la lista
- [ ] Ver detalle → muestra registro con contexto
- [ ] Asociar persona a empresa funciona
- [ ] Asignar rol (contacto/cliente/proveedor) funciona

### 3.4 Empresas (`/api/v1/empresas`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Búsqueda/filtro funciona
- [ ] Crear empresa → aparece en la lista
- [ ] Editar empresa → cambios reflejados
- [ ] Eliminar empresa → desaparece de la lista
- [ ] Ver detalle → muestra registro con contexto
- [ ] Asignar rol (cliente/proveedor) funciona

### 3.5 Proveedores (`/api/v1/proveedores`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Búsqueda/filtro funciona
- [ ] Crear proveedor → aparece en la lista
- [ ] Editar proveedor → cambios reflejados
- [ ] Eliminar proveedor → desaparece de la lista
- [ ] Ver detalle → muestra portafolio y solicitudes

### 3.6 Servicios (`/api/v1/servicios`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Filtro por categoría funciona
- [ ] Crear servicio → aparece en la lista
- [ ] Editar servicio → cambios reflejados
- [ ] Eliminar servicio → desaparece de la lista
- [ ] Ver detalle → muestra subservicios y descuentos/recargos
- [ ] Asignar subservicio a servicio padre funciona
- [ ] Categorizar servicio funciona

### 3.7 Cotizaciones (`/api/v1/cotizaciones`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Filtro por estado, cliente y fecha funciona
- [ ] Crear cotización → aparece en la lista con estado inicial
- [ ] Editar cotización → cambios reflejados
- [ ] Eliminar cotización → desaparece de la lista
- [ ] Ver detalle → muestra ítems, porcentajes y total

### 3.8 Eventos (`/api/v1/eventos`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Búsqueda/filtro funciona
- [ ] Crear evento (desde cotización aprobada) → aparece en lista
- [ ] Editar evento → cambios reflejados
- [ ] Eliminar evento → desaparece de la lista
- [ ] Ver detalle → muestra proveedores, servicios, personal, observaciones

### 3.9 Personal de Evento (`/api/v1/eventos/{id}/personal`)
- [ ] Vista de lista carga personal del evento
- [ ] Crear asignación de personal → aparece en lista
- [ ] Editar asignación → cambios reflejados
- [ ] Eliminar asignación → desaparece de la lista
- [ ] Ver detalle → muestra turno, valor, ARL, OP

### 3.10 Órdenes de Compra (`/api/v1/ordenes-compra`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Búsqueda/filtro funciona
- [ ] Crear orden de compra → aparece en la lista
- [ ] Editar orden → cambios reflejados
- [ ] Eliminar orden → desaparece de la lista
- [ ] Ver detalle → muestra relación con solicitud

### 3.11 Mensajes (`/api/v1/mensajes`)
- [ ] Vista de lista carga con datos
- [ ] Crear mensaje/plantilla → aparece en lista
- [ ] Editar mensaje → cambios reflejados
- [ ] Eliminar mensaje → desaparece de la lista

### 3.12 Presentaciones (`/api/v1/presentaciones`)
- [ ] Vista de lista carga con datos
- [ ] Paginación funciona
- [ ] Filtro funciona
- [ ] Crear presentación → aparece en lista
- [ ] Editar presentación → cambios reflejados
- [ ] Eliminar presentación → desaparece de la lista

### 3.13 Inventario (`/api/v1/inventario`)
- [ ] Vista principal muestra estado actual de existencias
- [ ] Registrar ingreso → stock se incrementa
- [ ] Registrar retiro → stock se decrementa
- [ ] Historial de ingresos visible
- [ ] Historial de retiros visible

### 3.14 Alimentación (`/api/v1/eventos/{id}/alimentacion`)
- [ ] Vista muestra estado actual de alimentación del evento
- [ ] Registrar ingreso → cantidad se incrementa
- [ ] Registrar retiro → cantidad se decrementa
- [ ] Historial de movimientos visible

### 3.15 Catálogos (`/api/v1/catalogos/{tipo}`)
- [ ] tipo-documento: listar, crear, editar, eliminar
- [ ] rol-entidad: listar, crear, editar, eliminar
- [ ] estado: listar, crear, editar, eliminar
- [ ] categoria-servicio: listar, crear, editar, eliminar
- [ ] unidad-medida: listar, crear, editar, eliminar
- [ ] rol-evento: listar, crear, editar, eliminar

---

## 4. Enforcement de Permisos (Requirement 1)

### 4.1 Backend — Bloqueo por API
- [ ] Usuario sin permiso "ver" en módulo → API responde 403
- [ ] Usuario sin permiso "crear" → POST al endpoint responde 403
- [ ] Usuario sin permiso "editar" → PUT al endpoint responde 403
- [ ] Usuario sin permiso "eliminar" → DELETE al endpoint responde 403
- [ ] El mensaje de error es: "No tiene permisos para realizar esta acción"

### 4.2 Frontend — Ocultamiento de UI
- [ ] Botón "Crear" oculto si usuario no tiene permiso "crear"
- [ ] Botón "Editar" oculto si usuario no tiene permiso "editar"
- [ ] Botón "Eliminar" oculto si usuario no tiene permiso "eliminar"
- [ ] Menú lateral NO muestra módulos sin permiso "ver"
- [ ] Secciones de contexto en vista detalle ocultas para tablas sin permiso

### 4.3 Roles predefinidos
- [ ] Rol Admin → acceso completo a todos los módulos y acciones
- [ ] Rol Comercial → acceso limitado (no puede eliminar, no accede a usuarios)
- [ ] Actualizar JSON de permisos de un rol → efecto inmediato en siguiente request

### 4.4 Configuración granular
- [ ] Asignar permiso por tabla: ver listado, ver detalle, crear, editar, eliminar
- [ ] Permisos se almacenan en formato JSON en tabla `permiso`
- [ ] Backend valida contra JSON en cada request

---

## 5. Verificación de Soft-Delete (Eliminación Lógica)

### 5.1 Comportamiento en frontend
- [ ] Al eliminar un registro → desaparece de la vista de lista
- [ ] El registro NO aparece al buscar/filtrar
- [ ] El registro NO aparece en selectores/dropdowns dependientes

### 5.2 Verificación en base de datos
- [ ] Registro eliminado existe en la tabla con `activo = false`
- [ ] Consulta SQL directa: `SELECT * FROM {tabla} WHERE activo = false` retorna el registro
- [ ] El campo `activo` cambió de `true` a `false`

### 5.3 Verificación por API
- [ ] GET al endpoint del módulo NO retorna registros con `activo = false`
- [ ] No existe endpoint público para recuperar registros eliminados (a menos que sea admin)

---

## 6. PostgreSQL Functions y Triggers

### 6.1 fn_recalcular_total_cotizacion
- [ ] Agregar ítem a cotización → total se recalcula automáticamente
- [ ] Modificar cantidad de ítem → total se actualiza
- [ ] Eliminar ítem → total se recalcula
- [ ] Aplicar porcentaje de descuento → total reflejado

### 6.2 fn_crear_evento_desde_cotizacion
- [ ] Ejecutar con cotización en estado APROBADA → evento se crea exitosamente
- [ ] Ejecutar con cotización en estado diferente a APROBADA → error
- [ ] El evento creado hereda datos de la cotización (servicios, clientes, etc.)
- [ ] POST a `/api/v1/cotizaciones/execute/crear_evento` con id válido → respuesta exitosa

### 6.3 fn_calcular_valor_turno
- [ ] Asignar turno a personal → valor calculado automáticamente
- [ ] El cálculo es correcto según parámetros (horas, tarifa, etc.)
- [ ] POST a `/api/v1/eventos/{id}/personal/execute/calcular_turno` → retorna valor correcto

### 6.4 trg_actualizar_stock (Inventario)
- [ ] Registrar ingreso → `stock_actual` se incrementa automáticamente
- [ ] Registrar retiro válido → `stock_actual` se decrementa
- [ ] Registrar retiro que excede stock disponible → error "No hay suficiente stock"
- [ ] Verificar en DB que el trigger actualizó el campo correctamente

### 6.5 trg_recalcular_subtotal_item
- [ ] Insertar ítem en cotización → subtotal se calcula automáticamente
- [ ] Actualizar cantidad o precio → subtotal se recalcula
- [ ] Verificar en DB que el trigger ejecutó el cálculo

### 6.6 Trigger de alimentación
- [ ] Ingreso de alimentación → cantidad disponible se actualiza
- [ ] Retiro de alimentación → cantidad disponible se actualiza
- [ ] Retiro que excede cantidad → error "No hay suficiente cantidad para este retiro"

---

## 7. Descargas PDF/Excel

### 7.1 Generación de PDF
- [ ] Descargar PDF de cotización → archivo válido se descarga
- [ ] PDF contiene datos correctos (ítems, totales, cliente, fecha)
- [ ] PDF tiene formato legible y profesional
- [ ] Descargar PDF de presentación → archivo válido se descarga

### 7.2 Generación de Excel
- [ ] Descargar Excel de órdenes de compra → archivo .xlsx válido
- [ ] Excel contiene columnas correctas con datos
- [ ] Descargar plantilla Excel de proveedores de un evento → archivo válido
- [ ] Descarga masiva de órdenes → Excel con múltiples registros

### 7.3 Validaciones de descarga
- [ ] Usuario sin permiso → endpoint de descarga responde 403
- [ ] Cotización inexistente → responde 404
- [ ] Archivo descargado se abre correctamente en visor PDF / Excel

---

## 8. Validación de Stock (Requirement 11)

### 8.1 Ingresos
- [ ] Registrar ingreso con cantidad, fecha, detalle y unidad de medida → éxito
- [ ] `stock_actual` se incrementa según la cantidad ingresada
- [ ] El movimiento aparece en historial de ingresos

### 8.2 Retiros válidos
- [ ] Registrar retiro con cantidad menor o igual a stock disponible → éxito
- [ ] `stock_actual` se decrementa correctamente
- [ ] El movimiento aparece en historial de retiros

### 8.3 Retiros inválidos
- [ ] Intentar retirar más de lo disponible → error
- [ ] Mensaje mostrado: "No hay suficiente stock para este retiro"
- [ ] El `stock_actual` NO se modifica tras intento fallido
- [ ] El error se muestra como alerta visible en la interfaz

### 8.4 Indicadores visuales
- [ ] Insumo con stock = 0 → muestra indicador visual "agotado"
- [ ] Insumo con stock bajo (si aplica) → indicador de advertencia

---

## 9. Validación de Alimentación (Requirement 12)

### 9.1 Ingresos
- [ ] Registrar ingreso de alimentación → cantidad disponible se incrementa
- [ ] Movimiento registrado con fecha y detalle
- [ ] Historial de ingresos muestra el movimiento

### 9.2 Retiros válidos
- [ ] Registrar retiro con cantidad disponible → éxito
- [ ] Cantidad disponible se decrementa correctamente
- [ ] Historial de retiros muestra el movimiento

### 9.3 Retiros inválidos
- [ ] Intentar retirar más de lo disponible → error
- [ ] Mensaje mostrado: "No hay suficiente cantidad para este retiro"
- [ ] Cantidades NO se modifican tras intento fallido

---

## 10. Flujo de Cotización Completo (Requirements 5-6)

### 10.1 Creación y configuración
- [ ] Crear cotización con ítems (servicios y cantidades) → éxito
- [ ] Asociar cliente/contacto a la cotización
- [ ] Asignar fecha de vencimiento

### 10.2 Cálculos automáticos
- [ ] Agregar/modificar ítems → total se recalcula (fn_recalcular_total_cotizacion)
- [ ] Aplicar porcentaje de descuento → total se actualiza
- [ ] Aplicar porcentaje de recargo → total se actualiza

### 10.3 Gestión de estados
- [ ] Cambiar estado de cotización → se actualiza correctamente
- [ ] Solo se muestran estados válidos para contexto "cotización"
- [ ] Cambiar a estado APROBADA → habilita creación de evento

### 10.4 Creación de evento desde cotización
- [ ] Desde cotización aprobada: "Crear evento" → evento se genera
- [ ] Evento hereda servicios, contactos y datos de la cotización
- [ ] Cotización no aprobada → botón "Crear evento" deshabilitado o error

### 10.5 Vencimientos
- [ ] Cotización próxima a vencer aparece en listado `/vencimientos`
- [ ] Notificación/indicador visual de cotización por vencer

---

## 11. Gestión de Catálogos (Requirement 13)

### 11.1 Operaciones CRUD por tipo
- [ ] tipo-documento: crear valor → aparece en selector al crear persona
- [ ] rol-entidad: crear valor → aparece en selector al asignar rol
- [ ] estado: crear valor con contexto → aparece en transiciones del módulo correspondiente
- [ ] categoria-servicio: crear valor → aparece en filtro de servicios
- [ ] unidad-medida: crear valor → aparece en selector de inventario
- [ ] rol-evento: crear valor → aparece al asignar personal a evento

### 11.2 Validaciones
- [ ] Actualizar valor de catálogo → reflejado en entidades dependientes
- [ ] Eliminar valor en uso (referenciado por FK) → error de validación
- [ ] Eliminar valor sin dependencias → eliminación exitosa
- [ ] Estados filtrados por contexto: solo se muestran estados válidos para la entidad

### 11.3 Integración con otros módulos
- [ ] Selector de tipo_documento al crear persona muestra opciones del catálogo
- [ ] Selector de rol_entidad al asignar rol muestra opciones del catálogo
- [ ] Selector de categoría en servicios carga desde catálogo
- [ ] Selector de unidad_medida en inventario carga desde catálogo
- [ ] Selector de estado muestra solo estados con contexto correcto

---

## 12. Validación de UI/Tema

### 12.1 Dark Mode
- [ ] Toggle de tema (dark/light) funciona correctamente
- [ ] Todos los componentes se renderizan correctamente en dark mode
- [ ] Textos legibles sobre fondos oscuros
- [ ] Botones y acciones distinguibles en modo oscuro
- [ ] Tablas, modales y formularios correctos en dark mode

### 12.2 Light Mode
- [ ] Todos los componentes se renderizan correctamente en light mode
- [ ] Contraste suficiente para lectura
- [ ] Iconos y badges visibles

### 12.3 Persistencia y preferencia del sistema
- [ ] Preferencia de tema persiste en `localStorage`
- [ ] Al recargar la página, el tema seleccionado se mantiene
- [ ] Primera visita: respeta `prefers-color-scheme` del sistema operativo

### 12.4 Animaciones
- [ ] Botones primarios tienen animación de press y hover
- [ ] Animaciones se deshabilitan con `prefers-reduced-motion: reduce`

---

## 13. Manejo de Errores

### 13.1 Mensajes en español
- [ ] Todos los mensajes de error visibles al usuario están en español
- [ ] No se muestran stack traces ni mensajes técnicos al usuario
- [ ] Errores de validación se muestran inline en campos del formulario

### 13.2 Errores HTTP
- [ ] Error 400 → "La solicitud contiene datos inválidos"
- [ ] Error 401 → "No tiene autorización. Inicie sesión nuevamente"
- [ ] Error 403 → "No tiene permisos para realizar esta acción"
- [ ] Error 404 → "El recurso solicitado no fue encontrado"
- [ ] Error 500 → "Ocurrió un error en el servidor. Intente más tarde"

### 13.3 Errores de conexión
- [ ] Sin conectividad → muestra "No se pudo conectar con el servidor. Verifique su conexión a internet"
- [ ] Backend caído → mensaje de error amigable, sin pantalla en blanco

### 13.4 Validaciones de formulario
- [ ] Campo requerido vacío → "Este campo es obligatorio"
- [ ] Email inválido → "Ingrese un correo electrónico válido"
- [ ] Longitud mínima no alcanzada → mensaje con la longitud requerida
- [ ] Formato inválido → "El formato ingresado no es válido"

---

## 14. Resumen de Ejecución

| Sección | Total Casos | Pasaron | Fallaron | Pendientes |
|---------|:-----------:|:-------:|:--------:|:----------:|
| 2. Autenticación | 12 | | | |
| 3. CRUD por Módulo | 95 | | | |
| 4. Permisos | 17 | | | |
| 5. Soft-Delete | 8 | | | |
| 6. Functions/Triggers | 18 | | | |
| 7. Descargas PDF/Excel | 10 | | | |
| 8. Stock | 10 | | | |
| 9. Alimentación | 8 | | | |
| 10. Cotización Workflow | 12 | | | |
| 11. Catálogos | 14 | | | |
| 12. UI/Tema | 12 | | | |
| 13. Errores | 14 | | | |
| **TOTAL** | **~180** | | | |

---

## 15. Notas y Hallazgos

### Problemas encontrados durante pruebas

| # | Sección | Descripción | Severidad | Estado |
|---|---------|-------------|-----------|--------|
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |

### Observaciones generales

- 
- 
- 

### Pasos manuales que requieren atención especial

1. **Verificación de soft-delete en DB:** Requiere acceso directo a PostgreSQL (usar pgAdmin o `psql`) para confirmar que `activo = false`.
2. **Expiración de JWT:** Para probar expiración, se puede reducir temporalmente el `jwt.expiration` en `application.yml` o esperar el tiempo configurado.
3. **Permisos inmediatos:** Tras modificar permisos JSON, verificar que el siguiente request ya valida contra el JSON actualizado (no hay caché).
4. **Triggers PostgreSQL:** Para confirmar ejecución de triggers, verificar los valores calculados directamente en la base de datos después de cada operación.
5. **Descarga de archivos:** Verificar que los archivos descargados se abren correctamente en un visor externo (Adobe Reader, Excel, LibreOffice).

---

*Documento generado como parte del Task 7.6 del plan de implementación del ERP Pro Arte.*
