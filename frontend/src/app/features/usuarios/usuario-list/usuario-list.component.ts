import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { UsuarioService } from '../usuario.service';
import { Usuario } from '../usuario.models';

@Component({
  selector: 'app-usuario-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
  templateUrl: './usuario-list.component.html',
  styleUrl: './usuario-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsuarioListComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(10);

  protected readonly showDeleteDialog = signal(false);
  private usuarioToDelete: Usuario | null = null;

  protected readonly columns: DataTableColumn[] = [
    { key: 'username', label: 'Usuario', sortable: true },
    { key: 'nombreCompleto', label: 'Nombre Completo', sortable: true },
    { key: 'email', label: 'Email', sortable: true },
    { key: 'rolNombre', label: 'Rol', sortable: true },
    { key: 'activo', label: 'Activo', sortable: true, type: 'boolean' },
    { key: 'createdAt', label: 'Fecha Creación', sortable: true, type: 'date' },
  ];

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: this.permissionService.hasPermission('usuario', 'ver_detalle'),
    editar: this.permissionService.hasPermission('usuario', 'editar'),
    eliminar: this.permissionService.hasPermission('usuario', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('usuario', 'crear');

  private currentParams: PageParams = { page: 0, size: 10 };

  ngOnInit(): void {
    this.loadUsuarios();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.currentParams = { ...this.currentParams, page };
    this.loadUsuarios();
  }

  protected onSortChange(event: SortEvent): void {
    this.currentParams = {
      ...this.currentParams,
      sort: `${event.column},${event.direction}`,
      page: 0,
    };
    this.currentPage.set(0);
    this.loadUsuarios();
  }

  protected onSearchChange(search: string): void {
    this.currentParams = { ...this.currentParams, search, page: 0 };
    this.currentPage.set(0);
    this.loadUsuarios();
  }

  protected onView(usuario: Usuario): void {
    this.router.navigate(['/usuarios', usuario.id, 'editar']);
  }

  protected onEdit(usuario: Usuario): void {
    this.router.navigate(['/usuarios', usuario.id, 'editar']);
  }

  protected onDelete(usuario: Usuario): void {
    this.usuarioToDelete = usuario;
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.usuarioToDelete) return;

    this.usuarioService.delete(this.usuarioToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.usuarioToDelete = null;
        this.loadUsuarios();
      },
      error: () => {
        this.showDeleteDialog.set(false);
        this.usuarioToDelete = null;
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.usuarioToDelete = null;
  }

  protected createUsuario(): void {
    this.router.navigate(['/usuarios', 'nuevo']);
  }

  private loadUsuarios(): void {
    this.loading.set(true);
    this.usuarioService.getAll(this.currentParams).subscribe({
      next: (response) => {
        this.usuarios.set(response.content);
        this.totalItems.set(response.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
