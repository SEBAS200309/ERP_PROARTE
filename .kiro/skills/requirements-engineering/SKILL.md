---
name: ingenieria-de-requerimientos
description: Transforma ideas vagas de funcionalidades en requerimientos claros y verificables usando el formato EARS, generando documentos compatibles con Kiro Specs.
license: MIT
compatibility: Kiro
metadata:
  category: metodologia
  complexity: principiante
  author: Kiro Team
  version: "2.0.0"
---

# Ingenieria de Requerimientos (Kiro Specs)

Domina el arte de capturar que se necesita construir antes de sumergirte en como construirlo. Esta skill usa el formato EARS (Easy Approach to Requirements Syntax) para crear requerimientos claros, verificables y sin ambiguedad, generando documentos compatibles con el formato de Kiro Specs.

## Formato de Archivo Requerido por Kiro

Los archivos de requerimientos en Kiro DEBEN seguir esta estructura exacta:

```markdown
# Requirements Document

## Introduction

[Descripcion general del proyecto o funcionalidad]

## Glossary

- **Termino**: Definicion del termino
- **Otro termino**: Definicion

## Requirements

### Requirement 1: [Nombre del requerimiento]

**User Story:** Como [rol], quiero [funcionalidad], para que [beneficio].

#### Acceptance Criteria

1. CUANDO [evento] ENTONCES el sistema DEBE [respuesta]
2. SI [condicion] ENTONCES el sistema DEBE [respuesta]

### Requirement 2: [Nombre del requerimiento]

**User Story:** Como [rol], quiero [funcionalidad], para que [beneficio].

#### Acceptance Criteria

1. CUANDO [evento] ENTONCES el sistema DEBE [respuesta]
```

### Reglas de Formato Obligatorias

1. El archivo DEBE comenzar con `# Requirements Document` (primera linea, sin BOM)
2. DEBE tener la seccion `## Introduction`
3. DEBE tener la seccion `## Glossary`
4. DEBE tener la seccion `## Requirements`
5. Cada requerimiento usa `### Requirement N: Nombre`
6. La User Story se escribe como `**User Story:** texto` (inline, NO como heading)
7. Los criterios de aceptacion usan heading `#### Acceptance Criteria`
8. Los criterios se escriben como lista numerada
9. El archivo DEBE guardarse en UTF-8 sin BOM

## Cuando Usar Esta Skill

Usa ingenieria de requerimientos cuando:
- Inicias cualquier nueva funcionalidad o proyecto
- Clarificas solicitudes ambiguas de stakeholders
- Creas criterios de aceptacion para historias de usuario
- Documentas el comportamiento del sistema para pruebas
- Aseguras que todos los miembros del equipo comparten el mismo entendimiento

## El Formato EARS

EARS provee patrones consistentes para escribir requerimientos que son especificos, verificables y sin ambiguedad.

### Patrones Basicos

**Evento-Respuesta (Mas Comun):**
```
CUANDO [evento disparador] ENTONCES [sistema] DEBE [respuesta requerida]
```

**Comportamiento Condicional:**
```
SI [precondicion se cumple] ENTONCES [sistema] DEBE [respuesta requerida]
```

**Condiciones Complejas:**
```
CUANDO [evento] Y [condicion adicional] ENTONCES [sistema] DEBE [respuesta]
```

**Condiciones Opcionales:**
```
CUANDO [evento] O [evento alternativo] ENTONCES [sistema] DEBE [respuesta]
```

### Patrones Avanzados

**Basado en Estado:**
```
CUANDO [sistema esta en estado especifico] ENTONCES [sistema] DEBE [comportamiento]
```

**Rendimiento:**
```
CUANDO [accion del usuario] ENTONCES [sistema] DEBE [responder en X segundos/milisegundos]
```

**Seguridad:**
```
SI [condicion de autenticacion] ENTONCES [sistema] DEBE [respuesta de seguridad]
```

## Proceso Paso a Paso

### Paso 1: Capturar Historias de Usuario

Formato: **Como [rol], quiero [funcionalidad], para que [beneficio]**

Enfocate en:
- Quien es el usuario (rol)
- Que quiere lograr (funcionalidad)
- Por que importa (beneficio/valor)

### Paso 2: Generar Criterios de Aceptacion

Para cada historia de usuario, define criterios de aceptacion especificos usando EARS.

Los criterios DEBEN:
- Ser una lista numerada bajo el heading `#### Acceptance Criteria`
- Usar el patron CUANDO/ENTONCES o SI/ENTONCES
- Incluir tanto el camino feliz como los casos de error
- Ser verificables y especificos

### Paso 3: Identificar Casos Borde

Para cada requerimiento, pregunta:
- Que pasa si la entrada esta vacia o es nula
- Que pasa si la entrada esta en valores limite
- Que pasa si la operacion falla
- Que pasa si el usuario no esta autorizado
- Que pasa si hay operaciones concurrentes

Los casos borde se incluyen como criterios adicionales dentro de `#### Acceptance Criteria` usando el patron SI/ENTONCES.

### Paso 4: Validar Requerimientos

Usa esta lista de verificacion:

**Completitud:**
- [ ] Todos los roles de usuario identificados y abordados
- [ ] Escenarios de flujo normal cubiertos
- [ ] Casos borde documentados como criterios SI/ENTONCES
- [ ] Casos de error manejados
- [ ] Reglas de negocio capturadas

**Claridad:**
- [ ] Cada requerimiento usa lenguaje preciso
- [ ] Sin terminos ambiguos (rapido, facil, amigable)
- [ ] Jerga tecnica definida en el Glossary
- [ ] Comportamientos esperados son especificos

**Consistencia:**
- [ ] Formato EARS usado en todo el documento
- [ ] Terminologia consistente entre requerimientos
- [ ] Sin requerimientos contradictorios
- [ ] Escenarios similares manejados de forma similar

**Verificabilidad:**
- [ ] Cada requerimiento puede ser verificado
- [ ] Criterios de exito son observables
- [ ] Entradas y salidas esperadas especificadas
- [ ] Requerimientos de rendimiento son medibles

**Formato Kiro:**
- [ ] Archivo comienza con `# Requirements Document`
- [ ] Secciones Introduction, Glossary y Requirements presentes
- [ ] Cada requirement tiene `**User Story:**` inline
- [ ] Cada requirement tiene `#### Acceptance Criteria` como heading
- [ ] Archivo en UTF-8 sin BOM

## Errores Comunes a Evitar

### Error 1: Requerimientos Vagos
**Mal:** "El sistema debe ser rapido"
**Bien:** "CUANDO el usuario envia una busqueda ENTONCES el sistema DEBE retornar resultados en menos de 2 segundos"

### Error 2: Detalles de Implementacion
**Mal:** "El sistema debe usar Redis para cache"
**Bien:** "CUANDO el usuario solicita datos de acceso frecuente ENTONCES el sistema DEBE retornar resultados cacheados"

### Error 3: Casos de Error Faltantes
**Mal:** Solo documentar el camino feliz
**Bien:** Incluir sentencias CUANDO/SI para todas las condiciones de error

### Error 4: Requerimientos No Verificables
**Mal:** "El sistema debe ser amigable con el usuario"
**Bien:** "CUANDO un nuevo usuario completa el onboarding ENTONCES el sistema DEBE requerir no mas de 3 clics para llegar al dashboard principal"

### Error 5: Formato Incorrecto para Kiro
**Mal:** Usar `#### User Story` como heading
**Bien:** Usar `**User Story:**` como texto en negrita inline

**Mal:** Usar `**Criterios de Aceptacion:**` como texto
**Bien:** Usar `#### Acceptance Criteria` como heading

## Ejemplo Completo Compatible con Kiro

```markdown
# Requirements Document

## Introduction

Sistema de gestion de tareas que permite a equipos crear, asignar y dar seguimiento a tareas de proyecto.

## Glossary

- **Tarea**: Unidad de trabajo asignable a un miembro del equipo
- **Sprint**: Periodo de tiempo fijo para completar un conjunto de tareas
- **Backlog**: Lista priorizada de tareas pendientes

## Requirements

### Requirement 1: Gestion de tareas

**User Story:** Como miembro del equipo, quiero crear y gestionar tareas, para organizar mi trabajo diario.

#### Acceptance Criteria

1. CUANDO el usuario crea una tarea ENTONCES el sistema DEBE almacenarla con titulo, descripcion y estado inicial "pendiente"
2. CUANDO el usuario actualiza una tarea ENTONCES el sistema DEBE reflejar los cambios inmediatamente
3. CUANDO el usuario elimina una tarea ENTONCES el sistema DEBE realizar eliminacion logica
4. CUANDO el usuario consulta tareas ENTONCES el sistema DEBE mostrar la lista con filtros por estado y asignado
5. SI el usuario no esta autenticado ENTONCES el sistema DEBE redirigir al login
6. SI el titulo de la tarea esta vacio ENTONCES el sistema DEBE mostrar "El titulo es obligatorio"

### Requirement 2: Asignacion de tareas

**User Story:** Como lider de equipo, quiero asignar tareas a miembros del equipo, para distribuir el trabajo equitativamente.

#### Acceptance Criteria

1. CUANDO el lider asigna una tarea a un miembro ENTONCES el sistema DEBE registrar la asignacion
2. CUANDO el lider consulta carga de trabajo ENTONCES el sistema DEBE mostrar tareas por miembro
3. SI el miembro asignado no existe ENTONCES el sistema DEBE mostrar error de validacion
4. CUANDO se asigna una tarea ENTONCES el sistema DEBE notificar al miembro asignado
```

## Proximos Pasos en Kiro

Despues de completar los requerimientos:
1. Revisar que el archivo no tenga diagnosticos (errores) en Kiro
2. Obtener aprobacion del usuario antes de proceder
3. Pasar a la Fase de Design (design.md) para crear la arquitectura tecnica
4. Luego generar Tasks (tasks.md) para la implementacion
5. Usar los requerimientos como base para pruebas de aceptacion
