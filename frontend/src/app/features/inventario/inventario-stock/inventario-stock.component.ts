import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { InventarioService } from '../inventario.service';
import { Insumo } from '../inventario.models';

@Component({
  selector: 'app-inventario-stock',
  standalone: true,
  imports: [DataTableComponent, AnimatedButtonComponent],
  templateUrl: './inventario-stock.component.html',
  styleUrl: './inventario-stock.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InventarioStockComponent implements OnInit {
  private readonly inventarioService = inject(InventarioService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly insumos = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly columns: DataTableColumn[] = [
    { key: 'nombre', label: 'Nombre', sortable: true },
    { key: 'descripcion', label: 'Descripción', sortable: false },
    { key: 'stockDisplay', label: 'Stock Actual', sortable: true },
    { key: 'updatedAt', label: 'Última Actualización', sortable: true, type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: this.permissionService.hasPermission('inventario', 'ver_detalle'),
    editar: false,
    eliminar: false,
  };

  protected readonly canCreateIngreso = this.permissionService.hasPermission('inventario', 'crear');
  protected readonly canCreateRetiro = this.permissionService.hasPermission('inventario', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadInsumos();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadInsumos();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadInsumos();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadInsumos();
  }

  protected onView(insumo: any): void {
    // Navigate to ingresos filtered by this insumo
    this.router.navigate(['/inventario/ingresos'], { queryParams: { insumoId: insumo.id } });
  }

  protected goToIngresos(): void {
    this.router.navigate(['/inventario/ingresos/nuevo']);
  }

  protected goToRetiros(): void {
    this.router.navigate(['/inventario/retiros/nuevo']);
  }

  protected goToIngresosHistory(): void {
    this.router.navigate(['/inventario/ingresos']);
  }

  protected goToRetirosHistory(): void {
    this.router.navigate(['/inventario/retiros']);
  }

  private loadInsumos(): void {
    this.loading.set(true);
    this.inventarioService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((insumo) => ({
          ...insumo,
          descripcion: insumo.descripcion || '—',
          stockDisplay: insumo.stockActual === 0 ? '0 ⛔ AGOTADO' : String(insumo.stockActual),
          isAgotado: insumo.stockActual === 0,
        }));
        this.insumos.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
