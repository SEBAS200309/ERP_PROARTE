import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { ServicioService } from '../servicio.service';
import { Servicio, CategoriaOption } from '../servicio.models';

@Component({
  selector: 'app-servicio-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent, FormsModule],
  templateUrl: './servicio-list.component.html',
  styleUrl: './servicio-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServicioListComponent implements OnInit {
  private readonly servicioService = inject(ServicioService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly servicios = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly categorias = signal<CategoriaOption[]>([]);
  protected readonly selectedCategoriaId = signal<string>('');

  protected readonly showDeleteDialog = signal(false);
  private servicioToDelete: Servicio | null = null;

  private categoriasMap = new Map<string, string>();

  protected readonly columns: DataTableColumn[] = [
    { key: 'nombre', label: 'Nombre', sortable: true },
    { key: 'categoriaNombre', label: 'Categoría', sortable: false },
    { key: 'tipoText', label: 'Tipo', sortable: false },
    { key: 'estadoText', label: 'Estado', sortable: false },
  ];

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: this.permissionService.hasPermission('servicio', 'ver_detalle'),
    editar: this.permissionService.hasPermission('servicio', 'editar'),
    eliminar: this.permissionService.hasPermission('servicio', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('servicio', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadCategorias();
  }

  protected onCategoriaChange(categoriaId: string): void {
    this.selectedCategoriaId.set(categoriaId);
    this.currentParams = {
      ...this.currentParams,
      categoriaId: categoriaId || undefined,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadServicios();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadServicios();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadServicios();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadServicios();
  }

  protected onView(servicio: any): void {
    this.router.navigate(['/servicios', servicio.id]);
  }

  protected onEdit(servicio: any): void {
    this.router.navigate(['/servicios', servicio.id, 'editar']);
  }

  protected onDelete(servicio: any): void {
    this.servicioToDelete = servicio;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.servicioToDelete) return;

    this.servicioService.delete(this.servicioToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.servicioToDelete = null;
        this.loadServicios();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.servicioToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.servicioToDelete = null;
  }

  protected createServicio(): void {
    this.router.navigate(['/servicios', 'nuevo']);
  }

  protected goToTree(): void {
    this.router.navigate(['/servicios', 'arbol']);
  }

  private loadCategorias(): void {
    this.servicioService.getCategorias().subscribe({
      next: (categorias) => {
        this.categorias.set(categorias);
        this.categoriasMap.clear();
        categorias.forEach((c) => this.categoriasMap.set(c.id, c.nombre));
        this.loadServicios();
      },
      error: () => {
        this.loadServicios();
      },
    });
  }

  private loadServicios(): void {
    this.loading.set(true);
    this.servicioService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((servicio) => ({
          ...servicio,
          categoriaNombre: servicio.categoriaId
            ? this.categoriasMap.get(servicio.categoriaId) || '—'
            : '—',
          tipoText: servicio.esPropio ? 'Propio' : 'Tercero',
          estadoText: servicio.activo ? 'Activo' : 'Inactivo',
        }));
        this.servicios.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
