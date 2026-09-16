import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { DataTableComponent, DataTableColumn, DataTablePermissions, SortEvent } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { PageParams } from '../../../core/models/pagination.model';
import { RolService } from '../rol.service';
import { Rol } from '../rol.models';

@Component({
    selector: 'app-rol-list',
    standalone: true,
    imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
    templateUrl: './rol-list.component.html',
    styleUrl: './rol-list.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RolListComponent implements OnInit {
    private readonly rolService = inject(RolService);
    private readonly permissionService = inject(PermissionService);
    private readonly router = inject(Router);

    protected readonly loading = signal(false);
    protected readonly roles = signal<Rol[]>([]);
    protected readonly totalItems = signal(0);
    protected readonly currentPage = signal(0);
    protected readonly pageSize = signal(10);
    protected readonly showDeleteDialog = signal(false);
    private rolToDelete: Rol | null = null;

    protected readonly columns: DataTableColumn[] = [
        { key: 'nombre', label: 'Nombre', sortable: true },
        { key: 'descripcion', label: 'Descripción', sortable: true },
        { key: 'createdAt', label: 'Fecha Creación', sortable: true, type: 'date' },
    ];

    protected readonly permissions: DataTablePermissions = {
        ver_detalle: this.permissionService.hasPermission('roles', 'ver_detalle'),
        leer: this.permissionService.hasPermission('roles', 'leer'),
        editar: this.permissionService.hasPermission('roles', 'editar'),
        eliminar: this.permissionService.hasPermission('roles', 'eliminar'),
    };

    protected readonly canCreate = this.permissionService.hasPermission('roles', 'crear');
    private currentParams: PageParams = { page: 0, size: 10 };

    ngOnInit(): void {
        this.loadRoles();
    }

    protected onPageChange(page: number): void {
        this.currentPage.set(page);
        this.currentParams = { ...this.currentParams, page };
        this.loadRoles();
    }

    protected onSortChange(event: SortEvent): void {
        this.currentParams = { ...this.currentParams, sort: `${event.column},${event.direction}`, page: 0 };
        this.currentPage.set(0);
        this.loadRoles();
    }

    protected onSearchChange(search: string): void {
        this.currentParams = { ...this.currentParams, nombre: search || undefined, page: 0 };
        this.currentPage.set(0);
        this.loadRoles();
    }

    protected onView(rol: Rol): void {
        this.router.navigate(['/roles', rol.id]);
    }

    protected onEdit(rol: Rol): void {
        this.router.navigate(['/roles', rol.id, 'editar']);
    }

    protected onDelete(rol: Rol): void {
        this.rolToDelete = rol;
        this.showDeleteDialog.set(true);
    }

    protected confirmDelete(): void {
        if (!this.rolToDelete) return;
        this.rolService.delete(this.rolToDelete.id).subscribe({
            next: () => {
                this.showDeleteDialog.set(false);
                this.rolToDelete = null;
                this.loadRoles();
            },
            error: () => {
                this.showDeleteDialog.set(false);
                this.rolToDelete = null;
            },
        });
    }

    protected cancelDelete(): void {
        this.showDeleteDialog.set(false);
        this.rolToDelete = null;
    }

    protected createRol(): void {
        this.router.navigate(['/roles', 'nuevo']);
    }

    private loadRoles(): void {
        this.loading.set(true);
        this.rolService.getAll(this.currentParams).subscribe({
            next: (response: any) => {
                this.roles.set(response.content || response);
                this.totalItems.set(response.totalElements || response.length);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            },
        });
    }
}