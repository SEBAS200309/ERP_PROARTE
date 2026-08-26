import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { PresentacionService } from '../presentacion.service';
import { Presentacion } from '../presentacion.models';

@Component({
  selector: 'app-presentacion-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
  templateUrl: './presentacion-list.component.html',
  styleUrl: './presentacion-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PresentacionListComponent implements OnInit {
  private readonly presentacionService = inject(PresentacionService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly presentaciones = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly showDeleteDialog = signal(false);
  private presentacionToDelete: Presentacion | null = null;

  protected readonly columns: DataTableColumn[] = [
    { key: 'titulo', label: 'Título', sortable: true },
    { key: 'descripcionTruncada', label: 'Descripción', sortable: false },
    { key: 'servicioNombre', label: 'Servicio', sortable: true },
    { key: 'createdAt', label: 'Fecha Creación', sortable: true, type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('presentaciones', 'leer'),
    editar: this.permissionService.hasPermission('presentaciones', 'editar'),
    eliminar: this.permissionService.hasPermission('presentaciones', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('presentaciones', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadPresentaciones();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadPresentaciones();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadPresentaciones();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadPresentaciones();
  }

  protected onView(presentacion: any): void {
    this.router.navigate(['/presentaciones', presentacion.id, 'editar']);
  }

  protected onEdit(presentacion: any): void {
    this.router.navigate(['/presentaciones', presentacion.id, 'editar']);
  }

  protected onDelete(presentacion: any): void {
    this.presentacionToDelete = presentacion;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.presentacionToDelete) return;

    this.presentacionService.delete(this.presentacionToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.presentacionToDelete = null;
        this.loadPresentaciones();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.presentacionToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.presentacionToDelete = null;
  }

  protected createPresentacion(): void {
    this.router.navigate(['/presentaciones', 'nuevo']);
  }

  private loadPresentaciones(): void {
    this.loading.set(true);
    this.presentacionService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((presentacion) => ({
          ...presentacion,
          descripcionTruncada: presentacion.descripcion
            ? presentacion.descripcion.length > 80
              ? presentacion.descripcion.substring(0, 80) + '...'
              : presentacion.descripcion
            : '—',
          servicioNombre: presentacion.servicioNombre || '—',
        }));
        this.presentaciones.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
