import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { ProveedorService } from '../proveedor.service';
import { SolicitudServicio, ServicioOption, EventoOption, CatalogoOption } from '../proveedor.models';

@Component({
  selector: 'app-solicitud-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
  templateUrl: './solicitud-list.component.html',
  styleUrl: './solicitud-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SolicitudListComponent implements OnInit {
  private readonly proveedorService = inject(ProveedorService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly solicitudes = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly showDeleteDialog = signal(false);
  private solicitudToDelete: SolicitudServicio | null = null;

  private proveedoresMap = new Map<string, string>();
  private serviciosMap = new Map<string, string>();
  private eventosMap = new Map<string, string>();
  private estadosMap = new Map<string, string>();

  protected readonly columns: DataTableColumn[] = [
    { key: 'proveedorNombre', label: 'Proveedor', sortable: false },
    { key: 'servicioNombre', label: 'Servicio', sortable: false },
    { key: 'eventoNombre', label: 'Evento', sortable: false },
    { key: 'estadoNombre', label: 'Estado', sortable: false },
    { key: 'createdAt', label: 'Fecha', sortable: true },
  ];

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: false,
    editar: this.permissionService.hasPermission('proveedor', 'editar'),
    eliminar: this.permissionService.hasPermission('proveedor', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('proveedor', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadCatalogos();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadSolicitudes();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadSolicitudes();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadSolicitudes();
  }

  protected onEdit(solicitud: any): void {
    this.router.navigate(['/proveedores', 'solicitudes', solicitud.id, 'editar']);
  }

  protected onDelete(solicitud: any): void {
    this.solicitudToDelete = solicitud;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.solicitudToDelete) return;

    this.proveedorService.deleteSolicitud(this.solicitudToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.solicitudToDelete = null;
        this.loadSolicitudes();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.solicitudToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.solicitudToDelete = null;
  }

  protected createSolicitud(): void {
    this.router.navigate(['/proveedores', 'solicitudes', 'nuevo']);
  }

  protected goBack(): void {
    this.router.navigate(['/proveedores']);
  }

  private loadCatalogos(): void {
    let loaded = 0;
    const checkReady = () => {
      loaded++;
      if (loaded >= 4) {
        this.loadSolicitudes();
      }
    };

    this.proveedorService.getAll({ page: 0, size: 200 }).subscribe({
      next: (response) => {
        this.proveedoresMap.clear();
        response.content.forEach((p) => {
          const nombre = p.especialidad || 'Proveedor';
          this.proveedoresMap.set(p.id, nombre);
        });
        checkReady();
      },
      error: () => checkReady(),
    });

    this.proveedorService.getServicios().subscribe({
      next: (servicios) => {
        this.serviciosMap.clear();
        servicios.forEach((s) => this.serviciosMap.set(s.id, s.nombre));
        checkReady();
      },
      error: () => checkReady(),
    });

    this.proveedorService.getEventos().subscribe({
      next: (eventos) => {
        this.eventosMap.clear();
        eventos.forEach((e) => this.eventosMap.set(e.id, e.nombre));
        checkReady();
      },
      error: () => checkReady(),
    });

    this.proveedorService.getEstados('solicitud').subscribe({
      next: (estados) => {
        this.estadosMap.clear();
        estados.forEach((e) => this.estadosMap.set(e.id, e.nombre));
        checkReady();
      },
      error: () => checkReady(),
    });
  }

  private loadSolicitudes(): void {
    this.loading.set(true);
    this.proveedorService.getSolicitudes(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((solicitud) => ({
          ...solicitud,
          proveedorNombre: this.proveedoresMap.get(solicitud.proveedorId) ?? '—',
          servicioNombre: this.serviciosMap.get(solicitud.servicioId) ?? '—',
          eventoNombre: solicitud.eventoId
            ? this.eventosMap.get(solicitud.eventoId) ?? '—'
            : '—',
          estadoNombre: solicitud.estadoId
            ? this.estadosMap.get(solicitud.estadoId) ?? '—'
            : '—',
          createdAt: solicitud.createdAt
            ? new Date(solicitud.createdAt).toLocaleDateString('es-CL')
            : '—',
        }));
        this.solicitudes.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
