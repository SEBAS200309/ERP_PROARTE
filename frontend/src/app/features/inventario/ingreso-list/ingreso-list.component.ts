import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { InventarioService } from '../inventario.service';
import { Insumo, InsumoOption } from '../inventario.models';

@Component({
  selector: 'app-ingreso-list',
  standalone: true,
  imports: [DataTableComponent, AnimatedButtonComponent],
  templateUrl: './ingreso-list.component.html',
  styleUrl: './ingreso-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IngresoListComponent implements OnInit {
  private readonly inventarioService = inject(InventarioService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly loading = signal(false);
  protected readonly movimientos = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly insumos = signal<InsumoOption[]>([]);
  protected readonly selectedInsumoId = signal<string>('');
  protected readonly selectedInsumoNombre = signal<string>('');

  protected readonly columns: DataTableColumn[] = [
    { key: 'cantidad', label: 'Cantidad', sortable: true, type: 'number' },
    { key: 'motivo', label: 'Motivo', sortable: false },
    { key: 'fecha', label: 'Fecha', sortable: true, type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('insumo', 'leer'),
    editar: this.permissionService.hasPermission('insumo', 'editar'),
    eliminar: this.permissionService.hasPermission('insumo', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('insumo', 'crear');


  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadInsumos();
    const insumoId = this.route.snapshot.queryParamMap.get('insumoId');
    if (insumoId) {
      this.selectedInsumoId.set(insumoId);
      this.loadMovimientos();
    }
  }

  protected onInsumoChange(insumoId: string): void {
    this.selectedInsumoId.set(insumoId);
    const insumo = this.insumos().find(i => i.id === insumoId);
    this.selectedInsumoNombre.set(insumo?.nombre || '');
    this.currentPage.set(0);
    this.currentParams = { ...this.currentParams, page: 0 };
    if (insumoId) {
      this.loadMovimientos();
    } else {
      this.movimientos.set([]);
      this.totalItems.set(0);
    }
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadMovimientos();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadMovimientos();
  }

  protected goToNuevoIngreso(): void {
    this.router.navigate(['/inventario/ingresos/nuevo']);
  }

  protected goBack(): void {
    this.router.navigate(['/inventario']);
  }

  private loadInsumos(): void {
    this.inventarioService.getAllInsumos().subscribe({
      next: (insumos) => {
        this.insumos.set(insumos.map(i => ({ id: i.id, nombre: i.nombre, stockActual: i.stockActual })));
        // If we have a pre-selected insumo, set the name
        const selectedId = this.selectedInsumoId();
        if (selectedId) {
          const insumo = insumos.find(i => i.id === selectedId);
          this.selectedInsumoNombre.set(insumo?.nombre || '');
        }
      },
      error: () => {
        this.insumos.set([]);
      },
    });
  }

  private loadMovimientos(): void {
    const insumoId = this.selectedInsumoId();
    if (!insumoId) return;

    this.loading.set(true);
    this.inventarioService.getMovimientos(insumoId, 'ingreso', this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((mov) => ({
          ...mov,
          motivo: mov.motivo || '—',
        }));
        this.movimientos.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
