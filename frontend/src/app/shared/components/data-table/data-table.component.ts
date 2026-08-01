import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  computed,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';

/** Column configuration for DataTable */
export interface DataTableColumn {
  key: string;
  label: string;
  sortable?: boolean;
  type?: 'text' | 'date' | 'number' | 'boolean';
}

/** Permissions for action buttons */
export interface DataTablePermissions {
  ver_detalle?: boolean;
  editar?: boolean;
  eliminar?: boolean;
}

/** Sort event payload */
export interface SortEvent {
  column: string;
  direction: 'asc' | 'desc';
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataTableComponent {
  // Inputs
  readonly columns = input.required<DataTableColumn[]>();
  readonly data = input.required<any[]>();
  readonly permissions = input<DataTablePermissions>({});
  readonly loading = input<boolean>(false);
  readonly totalItems = input<number>(0);
  readonly pageSize = input<number>(10);
  readonly currentPage = input<number>(0);
  readonly emptyMessage = input<string>('No se encontraron registros');

  // Outputs
  readonly viewAction = output<any>();
  readonly editAction = output<any>();
  readonly deleteAction = output<any>();
  readonly pageChange = output<number>();
  readonly sortChange = output<SortEvent>();
  readonly searchChange = output<string>();

  // Internal state
  protected readonly sortColumn = signal<string>('');
  protected readonly sortDirection = signal<'asc' | 'desc'>('asc');
  protected readonly searchTerm = signal<string>('');

  // Computed
  protected readonly totalPages = computed(() => {
    const total = this.totalItems();
    const size = this.pageSize();
    return size > 0 ? Math.ceil(total / size) : 0;
  });

  protected readonly hasActions = computed(() => {
    const perms = this.permissions();
    return perms.ver_detalle || perms.editar || perms.eliminar;
  });

  protected readonly skeletonRows = computed(() =>
    Array.from({ length: this.pageSize() }, (_, i) => i)
  );

  protected readonly hasPreviousPage = computed(() => this.currentPage() > 0);

  protected readonly hasNextPage = computed(
    () => this.currentPage() < this.totalPages() - 1
  );

  protected readonly pageDisplay = computed(
    () => `${this.currentPage() + 1} de ${this.totalPages() || 1}`
  );

  // Methods
  protected onSort(column: DataTableColumn): void {
    if (!column.sortable) return;

    const currentCol = this.sortColumn();
    const currentDir = this.sortDirection();

    if (currentCol === column.key) {
      const newDir = currentDir === 'asc' ? 'desc' : 'asc';
      this.sortDirection.set(newDir);
    } else {
      this.sortColumn.set(column.key);
      this.sortDirection.set('asc');
    }

    this.sortChange.emit({
      column: this.sortColumn(),
      direction: this.sortDirection(),
    });
  }

  protected onSearch(term: string): void {
    this.searchTerm.set(term);
    this.searchChange.emit(term);
  }

  protected onPreviousPage(): void {
    if (this.hasPreviousPage()) {
      this.pageChange.emit(this.currentPage() - 1);
    }
  }

  protected onNextPage(): void {
    if (this.hasNextPage()) {
      this.pageChange.emit(this.currentPage() + 1);
    }
  }

  protected onView(row: any): void {
    this.viewAction.emit(row);
  }

  protected onEdit(row: any): void {
    this.editAction.emit(row);
  }

  protected onDelete(row: any): void {
    this.deleteAction.emit(row);
  }

  protected getCellValue(row: any, column: DataTableColumn): string {
    const value = row[column.key];
    if (value == null) return '—';

    switch (column.type) {
      case 'date':
        return this.formatDate(value);
      case 'number':
        return this.formatNumber(value);
      case 'boolean':
        return value ? 'Sí' : 'No';
      default:
        return String(value);
    }
  }

  protected getSortIcon(column: DataTableColumn): string {
    if (!column.sortable) return '';
    if (this.sortColumn() !== column.key) return '⇅';
    return this.sortDirection() === 'asc' ? '▲' : '▼';
  }

  private formatDate(value: any): string {
    try {
      const date = new Date(value);
      return date.toLocaleDateString('es-CL');
    } catch {
      return String(value);
    }
  }

  private formatNumber(value: any): string {
    const num = Number(value);
    return isNaN(num) ? String(value) : num.toLocaleString('es-CL');
  }
}
