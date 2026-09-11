import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  computed,
  inject,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  trigger,
  transition,
  style,
  animate,
} from '@angular/animations';

import { DataTableColumn } from '../data-table/data-table.component';
import { PermissionService } from '../../../core/services/permission.service';

/** Configuration for a detail field */
export interface DetailField {
  key: string;
  label: string;
  type?: 'text' | 'date' | 'number' | 'boolean' | 'currency';
}

/** Configuration for a context section (related table) */
export interface DetailContextSection {
  tabla: string;
  title: string;
  columns: DataTableColumn[];
  data: any[];
}

@Component({
  selector: 'app-detail-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './detail-view.component.html',
  styleUrl: './detail-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('overlay', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('200ms ease-out', style({ opacity: 1 })),
      ]),
      transition(':leave', [
        animate('150ms ease-in', style({ opacity: 0 })),
      ]),
    ]),
    trigger('panel', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateX(100%)' }),
        animate('250ms ease-out', style({ opacity: 1, transform: 'translateX(0)' })),
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ opacity: 0, transform: 'translateX(100%)' })),
      ]),
    ]),
  ],
})
export class DetailViewComponent {
  // Inputs
  readonly visible = input<boolean>(false);
  readonly title = input<string>('Detalle');
  readonly record = input<Record<string, any> | null>(null);
  readonly fields = input<DetailField[]>([]);
  readonly contextSections = input<DetailContextSection[]>([]);

  // Outputs
  readonly closed = output<void>();

  // DI
  private readonly permissionService = inject(PermissionService);

  // Computed: filter context sections by user permissions
  protected readonly visibleSections = computed(() => {
    const sections = this.contextSections();
    return sections.filter((section) =>
      this.permissionService.hasPermission(section.tabla, 'leer')
    );
  });

  @HostListener('document:keydown.escape')
  protected onEscapeKey(): void {
    if (this.visible()) {
      this.close();
    }
  }

  protected close(): void {
    this.closed.emit();
  }

  protected onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('detail-view-overlay')) {
      this.close();
    }
  }

  protected getFieldValue(field: DetailField): string {
    const rec = this.record();
    if (!rec) return '—';

    const value = rec[field.key];
    if (value == null) return '—';

    switch (field.type) {
      case 'date':
        return this.formatDate(value);
      case 'number':
        return this.formatNumber(value);
      case 'currency':
        return this.formatCurrency(value);
      case 'boolean':
        return value ? 'Sí' : 'No';
      default:
        return String(value);
    }
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

  private formatCurrency(value: any): string {
    const num = Number(value);
    if (isNaN(num)) return String(value);
    return `$${num.toLocaleString('es-CL')}`;
  }
}
