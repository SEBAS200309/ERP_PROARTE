import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { EventoService } from '../evento.service';
import { Evento, EstadoOption } from '../evento.models';

@Component({
  selector: 'app-evento-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent, FormsModule],
  templateUrl: './evento-list.component.html',
  styleUrl: './evento-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventoListComponent implements OnInit {
  private readonly eventoService = inject(EventoService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly eventos = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly estados = signal<EstadoOption[]>([]);
  protected readonly selectedEstadoId = signal<string>('');

  protected readonly showDeleteDialog = signal(false);
  private eventoToDelete: Evento | null = null;

  private estadosMap = new Map<string, string>();

  protected readonly columns: DataTableColumn[] = [
    { key: 'nombre', label: 'Nombre', sortable: true },
    { key: 'lugar', label: 'Lugar', sortable: true },
    { key: 'fechaInicio', label: 'Fecha Inicio', sortable: true, type: 'date' },
    { key: 'fechaFin', label: 'Fecha Fin', sortable: true, type: 'date' },
    { key: 'estadoNombre', label: 'Estado', sortable: false },
  ];

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: this.permissionService.hasPermission('eventos', 'ver_detalle'),
    editar: this.permissionService.hasPermission('eventos', 'editar'),
    eliminar: this.permissionService.hasPermission('eventos', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('eventos', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadCatalogos();
  }

  protected onEstadoChange(estadoId: string): void {
    this.selectedEstadoId.set(estadoId);
    this.currentParams = { ...this.currentParams, estadoId: estadoId || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadEventos();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadEventos();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadEventos();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadEventos();
  }

  protected onView(evento: any): void {
    this.router.navigate(['/eventos', evento.id]);
  }

  protected onEdit(evento: any): void {
    this.router.navigate(['/eventos', evento.id]);
  }

  protected onDelete(evento: any): void {
    this.eventoToDelete = evento;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.eventoToDelete) return;

    this.eventoService.delete(this.eventoToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.eventoToDelete = null;
        this.loadEventos();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.eventoToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.eventoToDelete = null;
  }

  protected crearDesdeCotizacion(): void {
    const cotizacionId = prompt('Ingrese el ID de la cotización:');
    if (!cotizacionId) return;

    this.loading.set(true);
    this.eventoService.crearDesdeCotizacion(cotizacionId).subscribe({
      next: (evento) => {
        this.loading.set(false);
        this.router.navigate(['/eventos', evento.id]);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  private loadCatalogos(): void {
    this.eventoService.getEstados().subscribe({
      next: (estados) => {
        this.estados.set(estados);
        this.estadosMap.clear();
        estados.forEach((e) => this.estadosMap.set(e.id, e.nombre));
        this.loadEventos();
      },
      error: () => {
        this.loadEventos();
      },
    });
  }

  private loadEventos(): void {
    this.loading.set(true);
    this.eventoService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((evento) => ({
          ...evento,
          estadoNombre: evento.estadoId
            ? this.estadosMap.get(evento.estadoId) || '—'
            : '—',
        }));
        this.eventos.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
