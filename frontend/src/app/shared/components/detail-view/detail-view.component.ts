import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, transition, style, animate } from '@angular/animations';
import { AnimatedButtonComponent } from '../animated-button/animated-button.component';

export interface DetailField {
  key: string;
  label: string;
  type?: 'text' | 'date' | 'boolean';
}

export interface DetailContextSection {
  tabla: string;
  title: string;
  columns: any[];
  data: any[];
}

@Component({
  selector: 'app-detail-view',
  standalone: true,
  imports: [CommonModule, AnimatedButtonComponent],
  templateUrl: './detail-view.component.html',
  styleUrl: './detail-view.component.scss',
  animations: [
    trigger('overlay', [
      transition(':enter', [style({ opacity: 0 }), animate('200ms', style({ opacity: 1 }))]),
      transition(':leave', [animate('200ms', style({ opacity: 0 }))])
    ]),
    trigger('panel', [
      transition(':enter', [style({ transform: 'translateX(100%)' }), animate('250ms cubic-bezier(0.4, 0, 0.2, 1)', style({ transform: 'translateX(0)' }))]),
      transition(':leave', [animate('250ms cubic-bezier(0.4, 0, 0.2, 1)', style({ transform: 'translateX(100%)' }))])
    ])
  ]
})
export class DetailViewComponent {
  visible = input<boolean>(false);
  title = input<string>('Detalle de Registro');
  record = input<Record<string, any> | null>(null);
  fields = input<DetailField[]>([]);
  contextSections = input<DetailContextSection[]>([]);
  loading = input<boolean>(false);

  // Permisos para inyectar botones de acción
  permissions = input<{ editar?: boolean; eliminar?: boolean }>({});

  // Emisores de eventos
  closed = output<void>();
  edit = output<void>();
  delete = output<void>();

  close(): void {
    this.closed.emit();
  }

  onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('detail-view-overlay')) {
      this.close();
    }
  }

  getCellValue(row: any, col: any): string {
    return row[col.key] ?? '—';
  }

  getFieldValue(field: DetailField): string {
    const val = this.record()?.[field.key];
    if (val === null || val === undefined || val === '') return '—';
    if (field.type === 'date') return new Date(val).toLocaleDateString();
    if (field.type === 'boolean') return val ? 'Sí' : 'No';
    return String(val);
  }
}