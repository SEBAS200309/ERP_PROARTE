# Angular Frontend — Convenciones del Proyecto Pro Arte

Este documento define las convenciones de código, estilo visual y buenas prácticas para el frontend Angular del proyecto Pro Arte.

## Stack Frontend

- **Framework**: Angular 18+
- **Lenguaje**: TypeScript (strict mode)
- **Estilos**: SCSS
- **Temas**: Dark mode y Light mode obligatorios
- **Paleta de colores**: Gama de morados basada en el logo del proyecto
- **Animaciones**: Angular Animations para botones e interacciones

## Paleta de Colores — Gama Morado

Basada en el morado del logo del proyecto (`#6B3FA0`):

```scss
// === Variables de color principales ===
$primary-50: #F3E8FF;    // Fondo muy claro
$primary-100: #E9D5FF;   // Hover sutil
$primary-200: #D8B4FE;   // Bordes activos
$primary-300: #C084FC;   // Acentos secundarios
$primary-400: #A855F7;   // Hover de botones
$primary-500: #8B5CF6;   // Botones principales
$primary-600: #6B3FA0;   // COLOR LOGO — Primary brand
$primary-700: #5B21B6;   // Texto enfatizado
$primary-800: #4C1D95;   // Headers, dark surfaces
$primary-900: #3B0764;   // Fondo oscuro de alto contraste

// Colores complementarios del logo
$accent-gold: #F5A623;   // Amarillo/dorado del logo
$accent-red: #D0021B;    // Rojo del logo

// Neutrales
$neutral-50: #FAFAFA;
$neutral-100: #F5F5F5;
$neutral-200: #E5E5E5;
$neutral-300: #D4D4D4;
$neutral-700: #404040;
$neutral-800: #262626;
$neutral-900: #171717;
```

## Tema Dark / Light (Obligatorio)

Todo componente DEBE funcionar en ambos temas. Usa CSS custom properties:

```scss
// _themes.scss
:root, [data-theme="light"] {
  --color-bg-primary: #{$neutral-50};
  --color-bg-secondary: #{$neutral-100};
  --color-bg-surface: #FFFFFF;
  --color-text-primary: #{$neutral-900};
  --color-text-secondary: #{$neutral-700};
  --color-border: #{$neutral-200};
  --color-primary: #{$primary-600};
  --color-primary-hover: #{$primary-500};
  --color-primary-text: #FFFFFF;
  --color-accent: #{$accent-gold};
}

[data-theme="dark"] {
  --color-bg-primary: #{$neutral-900};
  --color-bg-secondary: #{$neutral-800};
  --color-bg-surface: #{$neutral-800};
  --color-text-primary: #{$neutral-50};
  --color-text-secondary: #{$neutral-300};
  --color-border: #{$neutral-700};
  --color-primary: #{$primary-400};
  --color-primary-hover: #{$primary-300};
  --color-primary-text: #{$neutral-900};
  --color-accent: #{$accent-gold};
}
```

Reglas:
- NUNCA usar colores hardcodeados en componentes — siempre `var(--color-xxx)`
- El tema se cambia con atributo `data-theme` en el `<html>` o `<body>`
- Crear un `ThemeService` que persista la preferencia del usuario en `localStorage`
- Respetar `prefers-color-scheme` del sistema como valor por defecto

## Animaciones de Botones

Todos los botones DEBEN tener animaciones usando Angular Animations:

```typescript
import { trigger, state, style, transition, animate } from '@angular/animations';

export const buttonAnimations = [
  trigger('buttonPress', [
    state('idle', style({ transform: 'scale(1)' })),
    state('pressed', style({ transform: 'scale(0.95)' })),
    transition('idle <=> pressed', animate('100ms ease-in-out'))
  ]),
  trigger('buttonHover', [
    state('out', style({ transform: 'translateY(0)', boxShadow: '0 2px 4px rgba(107, 63, 160, 0.2)' })),
    state('over', style({ transform: 'translateY(-2px)', boxShadow: '0 6px 12px rgba(107, 63, 160, 0.3)' })),
    transition('out <=> over', animate('200ms cubic-bezier(0.4, 0, 0.2, 1)'))
  ]),
  trigger('buttonRipple', [
    transition(':enter', [
      style({ opacity: 0.6, transform: 'scale(0)' }),
      animate('400ms ease-out', style({ opacity: 0, transform: 'scale(2.5)' }))
    ])
  ])
];
```

Reglas:
- Todo botón primario usa `buttonPress` + `buttonHover`
- Los botones secundarios usan al menos `buttonHover`
- Efecto ripple opcional para botones de acción importante
- Las animaciones DEBEN respetar `prefers-reduced-motion` — deshabilitarlas si el usuario lo pide

## Convención de Código — camelCase

### TypeScript
- Variables y funciones: `camelCase` → `getUserData`, `isLoading`, `totalItems`
- Clases e interfaces: `PascalCase` → `UserProfile`, `IAuthService`
- Constantes: `UPPER_SNAKE_CASE` → `MAX_RETRY_COUNT`, `API_BASE_URL`
- Enums: `PascalCase` con miembros en `PascalCase` → `UserRole.Admin`
- Archivos: `kebab-case` → `user-profile.component.ts` (convención Angular)

### HTML Templates
- Atributos custom y directivas: `camelCase` → `[userName]`, `(dataLoaded)`
- Variables de template: `camelCase` → `#userForm`, `let item`

### SCSS
- Variables: `$kebab-case` → `$primary-color` (convención SCSS estándar)
- Mixins: `kebab-case` → `@mixin flex-center`
- Clases CSS: `kebab-case` → `.user-card`, `.btn-primary`

## Mensajes de Error — Siempre en Español

TODOS los mensajes de error visibles al usuario DEBEN estar en español. Sin excepciones.

### Validaciones de formulario
```typescript
export const errorMessages: Record<string, string> = {
  required: 'Este campo es obligatorio',
  email: 'Ingrese un correo electrónico válido',
  minlength: 'Debe tener al menos {minlength} caracteres',
  maxlength: 'No puede exceder {maxlength} caracteres',
  pattern: 'El formato ingresado no es válido',
  min: 'El valor mínimo permitido es {min}',
  max: 'El valor máximo permitido es {max}',
};
```

### Errores HTTP
```typescript
export const httpErrorMessages: Record<number, string> = {
  400: 'La solicitud contiene datos inválidos',
  401: 'No tiene autorización. Inicie sesión nuevamente',
  403: 'No tiene permisos para realizar esta acción',
  404: 'El recurso solicitado no fue encontrado',
  409: 'Existe un conflicto con los datos actuales',
  422: 'Los datos enviados no pudieron ser procesados',
  500: 'Ocurrió un error en el servidor. Intente más tarde',
  503: 'El servicio no está disponible temporalmente',
  0: 'No se pudo conectar con el servidor. Verifique su conexión a internet',
};
```

### Mensajes genéricos
```typescript
export const generalMessages = {
  loadingError: 'Error al cargar los datos. Intente nuevamente',
  saveSuccess: 'Los cambios se guardaron correctamente',
  saveError: 'No se pudieron guardar los cambios',
  deleteConfirm: '¿Está seguro que desea eliminar este registro?',
  deleteSuccess: 'El registro se eliminó correctamente',
  deleteError: 'No se pudo eliminar el registro',
  sessionExpired: 'Su sesión ha expirado. Inicie sesión nuevamente',
  networkError: 'Error de conexión. Verifique su acceso a internet',
};
```

Reglas:
- Los logs técnicos (console) pueden estar en inglés
- Todo texto mostrado al usuario final: ESPAÑOL
- Usar un servicio centralizado `ErrorMessageService` para mapear errores
- Los mensajes de validación se resuelven desde un diccionario, nunca hardcodeados en templates

## Buenas Prácticas Angular (Guía Oficial 2024+)

### Estructura del Proyecto
- Organizar por feature areas, NO por tipo (`components/`, `services/`, etc.)
- Un concepto por archivo — un componente, directiva o servicio por archivo
- Tests (`.spec.ts`) junto al archivo que prueban, nunca en carpeta separada
- Archivos de componente comparten nombre: `user-profile.ts`, `user-profile.html`, `user-profile.scss`

```
src/
├── app/
│   ├── auth/
│   │   ├── login/
│   │   ├── register/
│   │   └── auth.service.ts
│   ├── dashboard/
│   │   ├── widgets/
│   │   └── dashboard.component.ts
│   ├── shared/
│   │   ├── components/
│   │   ├── directives/
│   │   └── pipes/
│   └── core/
│       ├── services/
│       ├── interceptors/
│       └── guards/
```

### Inyección de Dependencias
- Preferir `inject()` sobre inyección por constructor
- Usar `readonly` para propiedades inyectadas

```typescript
// ✅ Correcto
export class UserListComponent {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
}

// ❌ Evitar
export class UserListComponent {
  constructor(private userService: UserService) {}
}
```

### Componentes y Directivas
- Usar `standalone: true` en todos los componentes nuevos
- Inputs con `input()` signal — usar `readonly`
- Outputs con `output()` signal — usar `readonly`
- Usar `computed()` para valores derivados
- Usar `protected` para miembros usados solo en template
- Preferir `[class.xxx]` y `[style.xxx]` sobre `ngClass`/`ngStyle`
- Event handlers nombrados por acción: `saveUser()`, NO `handleClick()`
- Lifecycle hooks simples — delegar lógica a métodos con nombre descriptivo
- Implementar interfaces de lifecycle: `implements OnInit, OnDestroy`

```typescript
@Component({
  selector: 'app-user-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-card.component.html',
  styleUrl: './user-card.component.scss',
  animations: [buttonAnimations]
})
export class UserCardComponent implements OnInit {
  readonly userId = input.required<string>();
  readonly userDeleted = output<string>();

  private readonly userService = inject(UserService);

  protected readonly userName = computed(() => this.userData()?.name ?? '');

  ngOnInit(): void {
    this.loadUserData();
  }

  private loadUserData(): void {
    // ...
  }

  protected deleteUser(): void {
    this.userDeleted.emit(this.userId());
  }
}
```

### Selectores de Componentes
- Usar prefijo de aplicación: `app-` (o el prefijo definido en `angular.json`)
- Directivas: atributo camelCase con prefijo → `[appTooltip]`, `[appHighlight]`

### Rendimiento
- Lazy loading por feature module/routes
- `OnPush` change detection en componentes presentacionales
- `trackBy` en `@for` loops (o track expression en nueva sintaxis)
- Evitar cálculos pesados en templates — usar `computed()`

### Servicios
- Singleton con `providedIn: 'root'` para servicios globales
- Feature-scoped services en el `providers` del componente raíz del feature
- Retornar `Observable` o `Signal` desde servicios — nunca suscribirse internamente

### Routing
- Lazy load de rutas con `loadComponent` o `loadChildren`
- Guards como funciones (no clases)
- Resolvers para pre-carga de datos críticos

## Comandos

```bash
# Crear componente
ng generate component features/user-profile --standalone

# Crear servicio
ng generate service core/services/auth

# Build
ng build --configuration=production

# Tests
ng test --watch=false --browsers=ChromeHeadless

# Lint
ng lint

# Serve dev
ng serve --open
```

## Checklist para Nuevos Componentes

- [ ] Funciona en dark mode Y light mode
- [ ] Usa variables CSS (`var(--color-xxx)`) — nunca colores hardcodeados
- [ ] Botones tienen animaciones (`buttonPress`, `buttonHover`)
- [ ] Mensajes de error en español
- [ ] `standalone: true`
- [ ] Inyección con `inject()`
- [ ] `ChangeDetection.OnPush` si es presentacional
- [ ] Respeta `prefers-reduced-motion`
- [ ] Tests unitarios creados (`.spec.ts`)
