import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { PorcentajeService } from '../porcentaje.service';
import { DescuentoRecargo, CategoriaOption } from '../servicio.models';

@Component({
  selector: 'app-porcentaje-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
  templateUrl: './porcentaje-list.component.html',
  styleUrl: './porcentaje-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PorcentajeListComponent implements OnInit {
  private readonly porcentajeService = inject(PorcentajeService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly porcentajes = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly showDeleteDialog = signal(false);
  private porcentajeToDelete: DescuentoRecargo | null = null;

  private tiposMap = new Map<string, string>();

  protected readonly columns: DataTableColumn[] = [
    { key: 'nombre', label: 'Nombre', sortable: true },
    { key: 'valor', label: 'Valor (%)', sortable: true },
    { key: 'tipoNombre', label: 'Tipo', sortable: false },
    { key: 'estadoText', label: 'Estado', sortable: false },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('descuentos_recargos', 'leer'),
    editar: this.permissionService.hasPermission('descuentos_recargos', 'editar'),
    eliminar: this.permissionService.hasPermission('descuentos_recargos', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('descuentos_recargos', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadTipos();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadPorcentajes();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadPorcentajes();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadPorcentajes();
  }

  protected onView(porcentaje: any): void {
    this.router.navigate(['/descuentos-recargos', porcentaje.id, 'editar']);
  }

  protected onEdit(porcentaje: any): void {
    this.router.navigate(['/descuentos-recargos', porcentaje.id, 'editar']);
  }

  protected onDelete(porcentaje: any): void {
    this.porcentajeToDelete = porcentaje;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.porcentajeToDelete) return;

    this.porcentajeService.delete(this.porcentajeToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.porcentajeToDelete = null;
        this.loadPorcentajes();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.porcentajeToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.porcentajeToDelete = null;
  }

  protected createPorcentaje(): void {
    this.router.navigate(['/descuentos-recargos', 'nuevo']);
  }

  private loadTipos(): void {
    this.porcentajeService.getTipos().subscribe({
      next: (tipos) => {
        this.tiposMap.clear();
        tipos.forEach((t) => this.tiposMap.set(t.id, t.nombre));
        this.loadPorcentajes();
      },
      error: () => {
        this.loadPorcentajes();
      },
    });
  }

  private loadPorcentajes(): void {
    this.loading.set(true);
    this.porcentajeService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((item) => ({
          ...item,
          tipoNombre: this.tiposMap.get(item.tipoId) || '—',
          estadoText: item.activo ? 'Activo' : 'Inactivo',
        }));
        this.porcentajes.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
