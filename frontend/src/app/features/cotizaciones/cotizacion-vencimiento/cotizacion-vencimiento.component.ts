import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { DataTableComponent, DataTableColumn, DataTablePermissions } from '../../../shared/components/data-table/data-table.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { CotizacionService } from '../cotizacion.service';
import { Cotizacion, EstadoOption } from '../cotizacion.models';

@Component({
  selector: 'app-cotizacion-vencimiento',
  standalone: true,
  imports: [DataTableComponent, AnimatedButtonComponent, FormsModule],
  templateUrl: './cotizacion-vencimiento.component.html',
  styleUrl: './cotizacion-vencimiento.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CotizacionVencimientoComponent implements OnInit {
  private readonly cotizacionService = inject(CotizacionService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly cotizaciones = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly dias = signal(7);

  private estadosMap = new Map<string, string>();

  protected readonly columns: DataTableColumn[] = [
    { key: 'codigo', label: 'Código', sortable: true },
    { key: 'estadoNombre', label: 'Estado', sortable: false },
    { key: 'fechaVencimiento', label: 'Vencimiento', sortable: true, type: 'date' },
    { key: 'total', label: 'Total', sortable: false, type: 'number' },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('cotizaciones', 'leer'),
    crear: this.permissionService.hasPermission('cotizaciones', 'crear'),
    editar: this.permissionService.hasPermission('cotizaciones', 'editar'),
    eliminar: false,
  };

  ngOnInit(): void {
    this.loadEstados();
  }

  protected onDiasChange(dias: number): void {
    this.dias.set(dias);
    this.loadVencimientos();
  }

  protected onView(cotizacion: any): void {
    this.router.navigate(['/cotizaciones', cotizacion.id, 'editar']);
  }

  protected onEdit(cotizacion: any): void {
    this.router.navigate(['/cotizaciones', cotizacion.id, 'editar']);
  }

  protected goBack(): void {
    this.router.navigate(['/cotizaciones']);
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadVencimientos();
  }

  private loadEstados(): void {
    this.cotizacionService.getEstados().subscribe({
      next: (estados) => {
        this.estadosMap.clear();
        estados.forEach((e) => this.estadosMap.set(e.id, e.nombre));
        this.loadVencimientos();
      },
      error: () => {
        this.loadVencimientos();
      },
    });
  }

  private loadVencimientos(): void {
    this.loading.set(true);
    this.cotizacionService.getPorVencer(this.dias()).subscribe({
      next: (response) => {
        const enriched = response.content.map((cotizacion) => ({
          ...cotizacion,
          estadoNombre: cotizacion.estadoId
            ? this.estadosMap.get(cotizacion.estadoId) || '—'
            : '—',
        }));
        this.cotizaciones.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
