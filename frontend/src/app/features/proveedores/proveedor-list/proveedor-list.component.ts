import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { ProveedorService } from '../proveedor.service';
import { Proveedor, PersonaOption, EmpresaOption } from '../proveedor.models';

@Component({
  selector: 'app-proveedor-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
  templateUrl: './proveedor-list.component.html',
  styleUrl: './proveedor-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProveedorListComponent implements OnInit {
  private readonly proveedorService = inject(ProveedorService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly proveedores = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly showDeleteDialog = signal(false);
  private proveedorToDelete: Proveedor | null = null;

  private personasMap = new Map<string, string>();
  private empresasMap = new Map<string, string>();

  protected readonly columns: DataTableColumn[] = [
    { key: 'especialidad', label: 'Especialidad', sortable: true },
    { key: 'vinculacion', label: 'Vinculación', sortable: false },
    { key: 'estadoText', label: 'Estado', sortable: false },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('proveedores', 'leer'),
    editar: this.permissionService.hasPermission('proveedores', 'editar'),
    eliminar: this.permissionService.hasPermission('proveedores', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('proveedores', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadCatalogos();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadProveedores();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadProveedores();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadProveedores();
  }

  protected onView(proveedor: any): void {
    this.router.navigate(['/proveedores', proveedor.id, 'portafolio']);
  }

  protected onEdit(proveedor: any): void {
    this.router.navigate(['/proveedores', proveedor.id, 'editar']);
  }

  protected onDelete(proveedor: any): void {
    this.proveedorToDelete = proveedor;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.proveedorToDelete) return;

    this.proveedorService.delete(this.proveedorToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.proveedorToDelete = null;
        this.loadProveedores();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.proveedorToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.proveedorToDelete = null;
  }

  protected createProveedor(): void {
    this.router.navigate(['/proveedores', 'nuevo']);
  }

  protected goToPortafolio(proveedor: any): void {
    this.router.navigate(['/proveedores', proveedor.id, 'portafolio']);
  }

  protected goToSolicitudes(): void {
    this.router.navigate(['/proveedores', 'solicitudes']);
  }

  private loadCatalogos(): void {
    this.proveedorService.getPersonas().subscribe({
      next: (personas) => {
        this.personasMap.clear();
        personas.forEach((p) => this.personasMap.set(p.id, `${p.nombres} ${p.apellidos}`));
        this.loadEmpresas();
      },
    });
  }

  private loadEmpresas(): void {
    this.proveedorService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresasMap.clear();
        empresas.forEach((e) => this.empresasMap.set(e.id, e.razonSocial));
        this.loadProveedores();
      },
    });
  }

  private loadProveedores(): void {
    this.loading.set(true);
    this.proveedorService.getAllEmpresas(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((proveedor) => ({
          ...proveedor,
          vinculacion: this.getVinculacion(proveedor),
          estadoText: proveedor.activo ? 'Activo' : 'Inactivo',
        }));
        this.proveedores.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  private getVinculacion(proveedor: Proveedor): string {
    if (proveedor.personaId) {
      const nombre = this.personasMap.get(proveedor.personaId);
      return nombre ? `Persona: ${nombre}` : 'Persona: —';
    }
    if (proveedor.empresaId) {
      const nombre = this.empresasMap.get(proveedor.empresaId);
      return nombre ? `Empresa: ${nombre}` : 'Empresa: —';
    }
    return '—';
  }
}
