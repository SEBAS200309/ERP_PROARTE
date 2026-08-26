import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { DetailViewComponent, DetailField, DetailContextSection } from '../../../shared/components/detail-view/detail-view.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { PersonaService } from '../persona.service';
import { Persona, CatalogoOption } from '../persona.models';

@Component({
  selector: 'app-persona-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent, DetailViewComponent],
  templateUrl: './persona-list.component.html',
  styleUrl: './persona-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonaListComponent implements OnInit {
  private readonly personaService = inject(PersonaService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly personas = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly showDeleteDialog = signal(false);
  private personaToDelete: Persona | null = null;

  // Detail view
  protected readonly showDetail = signal(false);
  protected readonly selectedPersona = signal<Record<string, any> | null>(null);
  protected readonly detailContextSections = signal<DetailContextSection[]>([]);

  protected readonly columns: DataTableColumn[] = [
    { key: 'nombres', label: 'Nombres', sortable: true },
    { key: 'apellidos', label: 'Apellidos', sortable: true },
    { key: 'documento', label: 'Documento', sortable: true },
    { key: 'email', label: 'Email', sortable: true },
    { key: 'rolEntidadNombre', label: 'Rol', sortable: false },
  ];

  protected readonly detailFields: DetailField[] = [
    { key: 'nombres', label: 'Nombres' },
    { key: 'apellidos', label: 'Apellidos' },
    { key: 'tipoDocumentoNombre', label: 'Tipo Documento' },
    { key: 'documento', label: 'Documento' },
    { key: 'telefono', label: 'Teléfono' },
    { key: 'email', label: 'Email' },
    { key: 'direccion', label: 'Dirección' },
    { key: 'rolEntidadNombre', label: 'Rol Entidad' },
    { key: 'createdAt', label: 'Fecha Creación', type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('personas', 'leer'),
    editar: this.permissionService.hasPermission('personas', 'editar'),
    eliminar: this.permissionService.hasPermission('personas', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('personas', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };
  private rolesMap = new Map<string, string>();
  private tiposDocMap = new Map<string, string>();

  ngOnInit(): void {
    this.loadCatalogos();
    this.loadPersonas();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadPersonas();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadPersonas();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, nombre: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadPersonas();
  }

  protected onView(persona: Persona): void {
    const enriched: Record<string, any> = {
      ...persona,
      tipoDocumentoNombre: this.tiposDocMap.get(persona.tipoDocumentoId || '') || '—',
      rolEntidadNombre: this.rolesMap.get(persona.rolEntidadId || '') || '—',
    };
    this.selectedPersona.set(enriched);
    this.loadDetailContext(persona.id);
    this.showDetail.set(true);
  }

  protected onEdit(persona: Persona): void {
    this.router.navigate(['/personas', persona.id, 'editar']);
  }

  protected onDelete(persona: Persona): void {
    this.personaToDelete = persona;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.personaToDelete) return;

    this.personaService.delete(this.personaToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.personaToDelete = null;
        this.loadPersonas();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.personaToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.personaToDelete = null;
  }

  protected createPersona(): void {
    this.router.navigate(['/personas', 'nuevo']);
  }

  protected closeDetail(): void {
    this.showDetail.set(false);
    this.selectedPersona.set(null);
    this.detailContextSections.set([]);
  }

  private loadCatalogos(): void {
    this.personaService.getRolesEntidad().subscribe({
      next: (roles) => {
        this.rolesMap.clear();
        roles.forEach((r) => this.rolesMap.set(r.id, r.nombre));
      },
    });
    this.personaService.getTiposDocumento().subscribe({
      next: (tipos) => {
        this.tiposDocMap.clear();
        tipos.forEach((t) => this.tiposDocMap.set(t.id, t.nombre));
      },
    });
  }

  private loadPersonas(): void {
    this.loading.set(true);
    this.personaService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((persona) => ({
          ...persona,
          rolEntidadNombre: this.rolesMap.get(persona.rolEntidadId || '') || '—',
        }));
        this.personas.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  private loadDetailContext(personaId: string): void {
    const sections: DetailContextSection[] = [];

    this.personaService.getLeads(personaId).subscribe({
      next: (leads) => {
        sections.push({
          tabla: 'lead',
          title: 'Leads Asociados',
          columns: [
            { key: 'descripcion', label: 'Descripción' },
            { key: 'createdAt', label: 'Fecha', type: 'date' },
          ],
          data: leads,
        });
        this.detailContextSections.set([...sections]);
      },
    });

    this.personaService.getCotizaciones(personaId).subscribe({
      next: (cotizaciones) => {
        sections.push({
          tabla: 'cotizacion',
          title: 'Cotizaciones',
          columns: [
            { key: 'descripcion', label: 'Descripción' },
            { key: 'createdAt', label: 'Fecha', type: 'date' },
          ],
          data: cotizaciones,
        });
        this.detailContextSections.set([...sections]);
      },
    });
  }
}
