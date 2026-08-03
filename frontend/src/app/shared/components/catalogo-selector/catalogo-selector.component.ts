import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  input,
  output,
  signal,
  effect,
} from '@angular/core';

import { CatalogoService } from '../../../features/catalogos/catalogo.service';
import { CatalogoItem } from '../../../features/catalogos/catalogo.models';

/**
 * Componente shared reutilizable que carga opciones de un tipo de catálogo
 * y renderiza un dropdown (select) estilizado para dark/light theme.
 *
 * @example
 * ```html
 * <app-catalogo-selector
 *   tipo="rol-entidad"
 *   placeholder="Seleccione un rol"
 *   [selectedId]="selectedRolId()"
 *   (selectionChange)="onRolChange($event)"
 * />
 * ```
 */
@Component({
  selector: 'app-catalogo-selector',
  standalone: true,
  imports: [],
  templateUrl: './catalogo-selector.component.html',
  styleUrl: './catalogo-selector.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogoSelectorComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);

  // Inputs
  readonly tipo = input.required<string>();
  readonly contexto = input<string | undefined>(undefined);
  readonly placeholder = input<string>('Seleccione una opción');
  readonly selectedId = input<string | null>(null);

  // Output
  readonly selectionChange = output<string>();

  // Internal state
  protected readonly options = signal<CatalogoItem[]>([]);
  protected readonly loading = signal(false);

  constructor() {
    // Recargar opciones cuando cambie tipo o contexto
    effect(() => {
      const tipo = this.tipo();
      const contexto = this.contexto();
      if (tipo) {
        this.loadOptions(tipo, contexto);
      }
    });
  }

  ngOnInit(): void {
    // La carga se dispara por el effect en el constructor
  }

  protected onChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.selectionChange.emit(target.value);
  }

  private loadOptions(tipo: string, contexto?: string): void {
    this.loading.set(true);
    this.catalogoService.getAllByTipo(tipo, contexto).subscribe({
      next: (items) => {
        this.options.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.options.set([]);
        this.loading.set(false);
      },
    });
  }
}
