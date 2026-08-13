import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { CotizacionService } from '../cotizacion.service';
import { Cotizacion, EstadoOption, PersonaOption, EmpresaOption } from '../cotizacion.models';

@Component({
  selector: 'app-cotizacion-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent, FormsModule],
  templateUrl: './cotizacion-list.component.html',
  styleUrl: './cotizacion-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CotizacionListComponent implements OnInit {
  private readonly cotizacionService = inject(CotizacionService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly cotizaciones = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly estados = signal<EstadoOption[]>([]);
  protected readonly personas = signal<PersonaOption[]>([]);
  protected readonly empresas = signal<EmpresaOption[]>([]);

  protected readonly selectedEstadoId = signal<string>('');
  protected readonly selectedPersonaId = signal<string>('');
  protected readonly selectedEmpresaId = signal<string>('');

  protected readonly showDeleteDialog = signal(false);
  private cotizacionToDelete: Cotizacion | null = null;

  private estadosMap = new Map<string, string>();
  private personasMap = new Map<string, string>();
  private empresasMap = new Map<string, string>();

  protected readonly columns: DataTableColumn[] = [
    { key: 'codigo', label: 'Código', sortable: true },
    { key: 'estadoNombre', label: 'Estado', sortable: false },
    { key: 'clienteNombre', label: 'Cliente', sortable: false },
    { key: 'fechaVencimiento', label: 'Vencimiento', sortable: true, type: 'date' },
    { key: 'total', label: 'Total', sortable: true, type: 'number' },
  ];

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: this.permissionService.hasPermission('cotizaciones', 'ver_detalle'),
    editar: this.permissionService.hasPermission('cotizaciones', 'editar'),
    eliminar: this.permissionService.hasPermission('cotizaciones', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('cotizaciones', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadCatalogos();
  }

  protected onEstadoChange(estadoId: string): void {
    this.selectedEstadoId.set(estadoId);
    this.currentParams = { ...this.currentParams, estadoId: estadoId || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadCotizaciones();
  }

  protected onPersonaChange(personaId: string): void {
    this.selectedPersonaId.set(personaId);
    this.currentParams = { ...this.currentParams, personaId: personaId || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadCotizaciones();
  }

  protected onEmpresaChange(empresaId: string): void {
    this.selectedEmpresaId.set(empresaId);
    this.currentParams = { ...this.currentParams, empresaId: empresaId || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadCotizaciones();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadCotizaciones();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadCotizaciones();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadCotizaciones();
  }

  protected onView(cotizacion: any): void {
    this.router.navigate(['/cotizaciones', cotizacion.id, 'editar']);
  }

  protected onEdit(cotizacion: any): void {
    this.router.navigate(['/cotizaciones', cotizacion.id, 'editar']);
  }

  protected onDelete(cotizacion: any): void {
    this.cotizacionToDelete = cotizacion;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.cotizacionToDelete) return;

    this.cotizacionService.delete(this.cotizacionToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.cotizacionToDelete = null;
        this.loadCotizaciones();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.cotizacionToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.cotizacionToDelete = null;
  }

  protected createCotizacion(): void {
    this.router.navigate(['/cotizaciones', 'nuevo']);
  }

  protected goToVencimientos(): void {
    this.router.navigate(['/cotizaciones', 'vencimientos']);
  }

  private loadCatalogos(): void {
    this.cotizacionService.getEstados().subscribe({
      next: (estados) => {
        this.estados.set(estados);
        this.estadosMap.clear();
        estados.forEach((e) => this.estadosMap.set(e.id, e.nombre));
      },
    });

    this.cotizacionService.getPersonas().subscribe({
      next: (personas) => {
        this.personas.set(personas);
        this.personasMap.clear();
        personas.forEach((p) => this.personasMap.set(p.id, p.nombre));
      },
    });

    this.cotizacionService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresas.set(empresas);
        this.empresasMap.clear();
        empresas.forEach((e) => this.empresasMap.set(e.id, e.nombre));
        this.loadCotizaciones();
      },
      error: () => {
        this.loadCotizaciones();
      },
    });
  }

  private loadCotizaciones(): void {
    this.loading.set(true);
    this.cotizacionService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((cotizacion) => ({
          ...cotizacion,
          estadoNombre: cotizacion.estadoId
            ? this.estadosMap.get(cotizacion.estadoId) || '—'
            : '—',
          clienteNombre: this.getClienteNombre(cotizacion),
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

  private getClienteNombre(cotizacion: Cotizacion): string {
    if (cotizacion.personaId) {
      return this.personasMap.get(cotizacion.personaId) || '—';
    }
    if (cotizacion.empresaId) {
      return this.empresasMap.get(cotizacion.empresaId) || '—';
    }
    return '—';
  }
}
