import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { MensajeService } from '../mensaje.service';
import { Mensaje } from '../mensaje.models';

@Component({
  selector: 'app-mensaje-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
  templateUrl: './mensaje-list.component.html',
  styleUrl: './mensaje-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MensajeListComponent implements OnInit {
  private readonly mensajeService = inject(MensajeService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly mensajes = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly showDeleteDialog = signal(false);
  private mensajeToDelete: Mensaje | null = null;

  protected readonly columns: DataTableColumn[] = [
    { key: 'nombre', label: 'Nombre', sortable: true },
    { key: 'contenidoTruncado', label: 'Contenido', sortable: false },
    { key: 'createdAt', label: 'Fecha Creación', sortable: true, type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: this.permissionService.hasPermission('mensajes', 'ver_detalle'),
    editar: this.permissionService.hasPermission('mensajes', 'editar'),
    eliminar: this.permissionService.hasPermission('mensajes', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('mensajes', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadMensajes();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadMensajes();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadMensajes();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search: search || undefined, page: 0 };
    this.currentPage.set(0);
    this.loadMensajes();
  }

  protected onView(mensaje: any): void {
    this.router.navigate(['/mensajes', mensaje.id, 'editar']);
  }

  protected onEdit(mensaje: any): void {
    this.router.navigate(['/mensajes', mensaje.id, 'editar']);
  }

  protected onDelete(mensaje: any): void {
    this.mensajeToDelete = mensaje;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.mensajeToDelete) return;

    this.mensajeService.delete(this.mensajeToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.mensajeToDelete = null;
        this.loadMensajes();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.mensajeToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.mensajeToDelete = null;
  }

  protected createMensaje(): void {
    this.router.navigate(['/mensajes', 'nuevo']);
  }

  private loadMensajes(): void {
    this.loading.set(true);
    this.mensajeService.getAll(this.currentParams).subscribe({
      next: (response) => {
        const enriched = response.content.map((mensaje) => ({
          ...mensaje,
          contenidoTruncado: mensaje.contenido
            ? mensaje.contenido.length > 80
              ? mensaje.contenido.substring(0, 80) + '...'
              : mensaje.contenido
            : '—',
        }));
        this.mensajes.set(enriched);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
