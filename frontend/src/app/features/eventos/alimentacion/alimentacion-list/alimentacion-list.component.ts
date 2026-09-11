import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../../shared/components/data-table/data-table.component';
import { AnimatedButtonComponent } from '../../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../../core/services/permission.service';
import { PageParams } from '../../../../core/models/pagination.model';
import { AlimentacionService } from '../alimentacion.service';
import { Alimentacion } from '../alimentacion.models';

@Component({
  selector: 'app-alimentacion-list',
  standalone: true,
  imports: [DataTableComponent, AnimatedButtonComponent],
  templateUrl: './alimentacion-list.component.html',
  styleUrl: './alimentacion-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AlimentacionListComponent implements OnInit {
  private readonly alimentacionService = inject(AlimentacionService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly loading = signal(false);
  protected readonly movimientos = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly tipoFiltro = signal<string | undefined>(undefined);

  protected eventoId = '';

  protected readonly columns: DataTableColumn[] = [
    { key: 'tipoMovimiento', label: 'Tipo', sortable: true },
    { key: 'descripcion', label: 'Descripción', sortable: false },
    { key: 'cantidad', label: 'Cantidad', sortable: true },
    { key: 'fecha', label: 'Fecha', sortable: true, type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('alimentacion', 'leer'),
    editar: this.permissionService.hasPermission('alimentacion', 'editar'),
    eliminar: this.permissionService.hasPermission('alimentacion', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('alimentacion', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.eventoId = this.route.snapshot.paramMap.get('id') ?? '';
    this.loadMovimientos();
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

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadMovimientos();
  }

  protected filterByTipo(tipo: string | undefined): void {
    this.tipoFiltro.set(tipo);
    this.currentPage.set(0);
    this.currentParams = { ...this.currentParams, page: 0 };
    this.loadMovimientos();
  }

  protected goToNuevoIngreso(): void {
    this.router.navigate(['/eventos', this.eventoId, 'alimentacion', 'ingresos', 'nuevo']);
  }

  protected goToNuevoRetiro(): void {
    this.router.navigate(['/eventos', this.eventoId, 'alimentacion', 'retiros', 'nuevo']);
  }

  protected goBackToEvento(): void {
    this.router.navigate(['/eventos', this.eventoId]);
  }

  private loadMovimientos(): void {
    this.loading.set(true);
    this.alimentacionService.getByEvento(this.eventoId, this.tipoFiltro(), this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((mov) => ({
          ...mov,
          descripcion: mov.descripcion || '—',
          tipoMovimiento: mov.tipoMovimiento === 'ingreso' ? '📥 Ingreso' : '📤 Retiro',
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
