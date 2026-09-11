import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { OrdenCompraService } from '../orden-compra.service';
import { OrdenCompra, EstadoOption, SolicitudOption } from '../orden-compra.models';

@Component({
  selector: 'app-orden-compra-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent, FormsModule],
  templateUrl: './orden-compra-list.component.html',
  styleUrl: './orden-compra-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdenCompraListComponent implements OnInit {
  private readonly ordenCompraService = inject(OrdenCompraService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly ordenes = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly estados = signal<EstadoOption[]>([]);
  protected readonly solicitudes = signal<SolicitudOption[]>([]);

  protected readonly selectedEstadoId = signal<string>('');
  protected readonly selectedSolicitudId = signal<string>('');

  protected readonly showDeleteDialog = signal(false);
  private ordenToDelete: OrdenCompra | null = null;

  private estadosMap = new Map<string, string>();
  private solicitudesMap = new Map<string, string>();

  protected readonly columns: DataTableColumn[] = [
    { key: 'codigo', label: 'Código', sortable: true },
    { key: 'estadoNombre', label: 'Estado', sortable: false },
    { key: 'descripcion', label: 'Descripción', sortable: false },
    { key: 'monto', label: 'Monto', sortable: true, type: 'number' },
    { key: 'solicitudNombre', label: 'Solicitud', sortable: false },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('ordenes_compra', 'leer'),
    editar: this.permissionService.hasPermission('ordenes_compra', 'editar'),
    eliminar: this.permissionService.hasPermission('ordenes_compra', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('ordenes_compra', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadCatalogos();
  }

  protected onEstadoChange(estadoId: string): void {
    this.selectedEstadoId.set(estadoId);
    this.currentParams = { ...this.currentParams, estadoId: estadoId || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadOrdenes();
  }

  protected onSolicitudChange(solicitudId: string): void {
    this.selectedSolicitudId.set(solicitudId);
    this.currentParams = { ...this.currentParams, solicitudId: solicitudId || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadOrdenes();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadOrdenes();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadOrdenes();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadOrdenes();
  }

  protected onView(orden: any): void {
    this.router.navigate(['/ordenes-compra', orden.id, 'editar']);
  }

  protected onEdit(orden: any): void {
    this.router.navigate(['/ordenes-compra', orden.id, 'editar']);
  }

  protected onDelete(orden: any): void {
    this.ordenToDelete = orden;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.ordenToDelete) return;

    this.ordenCompraService.delete(this.ordenToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.ordenToDelete = null;
        this.loadOrdenes();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.ordenToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.ordenToDelete = null;
  }

  protected createOrden(): void {
    this.router.navigate(['/ordenes-compra', 'nuevo']);
  }

  protected descargarExcel(): void {
    const estadoId = this.selectedEstadoId() || undefined;
    this.ordenCompraService.descargarExcel(estadoId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'ordenes-compra.xlsx';
        link.click();
        window.URL.revokeObjectURL(url);
      },
    });
  }

  private loadCatalogos(): void {
    this.ordenCompraService.getEstados().subscribe({
      next: (estados) => {
        this.estados.set(estados);
        this.estadosMap.clear();
        estados.forEach((e) => this.estadosMap.set(e.id, e.nombre));
      },
    });

    this.ordenCompraService.getSolicitudes().subscribe({
      next: (solicitudes) => {
        this.solicitudes.set(solicitudes);
        this.solicitudesMap.clear();
        solicitudes.forEach((s) => this.solicitudesMap.set(s.id, s.nombre));
        this.loadOrdenes();
      },
      error: () => {
        this.loadOrdenes();
      },
    });
  }

  private loadOrdenes(): void {
    this.loading.set(true);
    this.ordenCompraService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((orden) => ({
          ...orden,
          estadoNombre: orden.estadoId
            ? this.estadosMap.get(orden.estadoId) || '—'
            : '—',
          solicitudNombre: orden.solicitudId
            ? this.solicitudesMap.get(orden.solicitudId) || '—'
            : '—',
        }));
        this.ordenes.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
