import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { LeadService } from '../lead.service';
import { Lead, EstadoCatalogo } from '../lead.models';

@Component({
  selector: 'app-lead-list',
  standalone: true,
  imports: [FormsModule, DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
  templateUrl: './lead-list.component.html',
  styleUrl: './lead-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LeadListComponent implements OnInit {
  private readonly leadService = inject(LeadService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly leads = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly estados = signal<EstadoCatalogo[]>([]);
  protected readonly selectedEstadoId = signal<string>('');

  protected readonly showDeleteDialog = signal(false);
  private leadToDelete: Lead | null = null;

  protected readonly columns: DataTableColumn[] = [
    { key: 'descripcion', label: 'Descripción', sortable: true },
    { key: 'estadoNombre', label: 'Estado', sortable: true },
    { key: 'createdAt', label: 'Fecha Creación', sortable: true, type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    leer: this.permissionService.hasPermission('leads', 'leer'),
    editar: this.permissionService.hasPermission('leads', 'editar'),
    eliminar: this.permissionService.hasPermission('leads', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('leads', 'crear');


  private currentParams: PageParams = { page: 0, size: 10 };
  private estadosMap = new Map<string, string>();

  ngOnInit(): void {
    this.loadEstados();
    this.loadLeads();
  }

  protected onEstadoFilterChange(estadoId: string): void {
    this.selectedEstadoId.set(estadoId);
    this.currentParams = { ...this.currentParams, estadoId: estadoId || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadLeads();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadLeads();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadLeads();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search, page: 0 };
    this.currentPage.set(0);
    this.loadLeads();
  }

  protected onView(lead: Lead): void {
    this.router.navigate(['/leads', lead.id, 'editar']);
  }

  protected onEdit(lead: Lead): void {
    this.router.navigate(['/leads', lead.id, 'editar']);
  }

  protected onDelete(lead: Lead): void {
    this.leadToDelete = lead;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.leadToDelete) return;

    this.leadService.delete(this.leadToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.leadToDelete = null;
        this.loadLeads();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.leadToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.leadToDelete = null;
  }

  protected createLead(): void {
    this.router.navigate(['/leads', 'nuevo']);
  }

  private loadEstados(): void {
    this.leadService.getEstados().subscribe({
      next: (estados) => {
        this.estados.set(estados);
        this.estadosMap.clear();
        estados.forEach((e) => this.estadosMap.set(e.id, e.nombre));
      },
    });
  }

  private loadLeads(): void {
    this.loading.set(true);
    this.leadService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((lead) => ({
          ...lead,
          estadoNombre: this.estadosMap.get(lead.estadoId) || '—',
        }));
        this.leads.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
