import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { DetailViewComponent, DetailField, DetailContextSection } from '../../../shared/components/detail-view/detail-view.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { EmpresaService } from '../empresa.service';
import { Empresa, CatalogoOption } from '../empresa.models';

@Component({
  selector: 'app-empresa-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent, DetailViewComponent],
  templateUrl: './empresa-list.component.html',
  styleUrl: './empresa-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmpresaListComponent implements OnInit {
  private readonly empresaService = inject(EmpresaService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly empresas = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly showDeleteDialog = signal(false);
  private empresaToDelete: Empresa | null = null;

  // Detail view
  protected readonly showDetail = signal(false);
  protected readonly selectedEmpresa = signal<Record<string, any> | null>(null);
  protected readonly detailContextSections = signal<DetailContextSection[]>([]);

  protected readonly columns: DataTableColumn[] = [
    { key: 'razonSocial', label: 'Razón Social', sortable: true },
    { key: 'nit', label: 'NIT', sortable: true },
    { key: 'email', label: 'Email', sortable: true },
    { key: 'telefono', label: 'Teléfono', sortable: false },
    { key: 'rolEntidadNombre', label: 'Rol', sortable: false },
  ];

  protected readonly detailFields: DetailField[] = [
    { key: 'razonSocial', label: 'Razón Social' },
    { key: 'nit', label: 'NIT' },
    { key: 'direccion', label: 'Dirección' },
    { key: 'telefono', label: 'Teléfono' },
    { key: 'email', label: 'Email' },
    { key: 'rolEntidadNombre', label: 'Rol Entidad' },
    { key: 'createdAt', label: 'Fecha Creación', type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: this.permissionService.hasPermission('empresa', 'ver_detalle'),
    editar: this.permissionService.hasPermission('empresa', 'editar'),
    eliminar: this.permissionService.hasPermission('empresa', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('empresa', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };
  private rolesMap = new Map<string, string>();

  ngOnInit(): void {
    this.loadCatalogos();
    this.loadEmpresas();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadEmpresas();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadEmpresas();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, razonSocial: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadEmpresas();
  }

  protected onView(empresa: Empresa): void {
    const enriched: Record<string, any> = {
      ...empresa,
      rolEntidadNombre: this.rolesMap.get(empresa.rolEntidadId || '') || '—',
    };
    this.selectedEmpresa.set(enriched);
    this.loadDetailContext(empresa.id);
    this.showDetail.set(true);
  }

  protected onEdit(empresa: Empresa): void {
    this.router.navigate(['/empresas', empresa.id, 'editar']);
  }

  protected onDelete(empresa: Empresa): void {
    this.empresaToDelete = empresa;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.empresaToDelete) return;

    this.empresaService.delete(this.empresaToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.empresaToDelete = null;
        this.loadEmpresas();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.empresaToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.empresaToDelete = null;
  }

  protected createEmpresa(): void {
    this.router.navigate(['/empresas', 'nuevo']);
  }

  protected closeDetail(): void {
    this.showDetail.set(false);
    this.selectedEmpresa.set(null);
    this.detailContextSections.set([]);
  }

  private loadCatalogos(): void {
    this.empresaService.getRolesEntidad().subscribe({
      next: (roles) => {
        this.rolesMap.clear();
        roles.forEach((r) => this.rolesMap.set(r.id, r.nombre));
      },
    });
  }

  private loadEmpresas(): void {
    this.loading.set(true);
    this.empresaService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((empresa) => ({
          ...empresa,
          rolEntidadNombre: this.rolesMap.get(empresa.rolEntidadId || '') || '—',
        }));
        this.empresas.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  private loadDetailContext(empresaId: string): void {
    this.empresaService.getPersonasAsociadas(empresaId).subscribe({
      next: (personas) => {
        const sections: DetailContextSection[] = [
          {
            tabla: 'persona',
            title: 'Personas Asociadas',
            columns: [
              { key: 'nombres', label: 'Nombres' },
              { key: 'apellidos', label: 'Apellidos' },
              { key: 'documento', label: 'Documento' },
              { key: 'email', label: 'Email' },
            ],
            data: personas,
          },
        ];
        this.detailContextSections.set(sections);
      },
    });
  }
}
