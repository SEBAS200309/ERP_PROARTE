import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { DataTableComponent, DataTableColumn, DataTablePermissions } from '../../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';
import { CatalogoService } from '../catalogo.service';
import { CatalogoItem, TipoCatalogo, TIPOS_CATALOGO } from '../catalogo.models';

@Component({
  selector: 'app-catalogo-list',
  standalone: true,
  imports: [DataTableComponent, ConfirmDialogComponent, AnimatedButtonComponent],
  templateUrl: './catalogo-list.component.html',
  styleUrl: './catalogo-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogoListComponent implements OnInit {
  private readonly catalogoService = inject(CatalogoService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly loading = signal(false);
  protected readonly items = signal<any[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly currentPage = signal(0);
  protected readonly pageSize = signal(50);

  protected readonly showDeleteDialog = signal(false);
  protected readonly deleteErrorMessage = signal<string | null>(null);
  private itemToDelete: CatalogoItem | null = null;

  protected readonly tiposCatalogo = TIPOS_CATALOGO;
  protected readonly selectedTipo = signal<TipoCatalogo | null>(null);

  protected readonly selectedTipoLabel = computed(() => {
    const tipo = this.selectedTipo();
    if (!tipo) return 'Catálogos';
    const found = TIPOS_CATALOGO.find((t) => t.tipo === tipo);
    return found ? found.label : 'Catálogos';
  });

  protected readonly columns = computed<DataTableColumn[]>(() => {
    const tipo = this.selectedTipo();
    const baseCols: DataTableColumn[] = [
      { key: 'nombre', label: 'Nombre', sortable: true },
    ];

    if (tipo === 'unidad-medida') {
      baseCols.push({ key: 'abreviatura', label: 'Abreviatura', sortable: true });
    }
    if (tipo === 'estado') {
      baseCols.push({ key: 'contexto', label: 'Contexto', sortable: true });
    }

    return baseCols;
  });

  protected readonly permissions: DataTablePermissions = {
    ver_detalle: this.permissionService.hasPermission('catalogo', 'ver_detalle'),
    editar: this.permissionService.hasPermission('catalogo', 'editar'),
    eliminar: this.permissionService.hasPermission('catalogo', 'eliminar'),
  };

  protected readonly canCreate = this.permissionService.hasPermission('catalogo', 'crear');

  ngOnInit(): void {
    const tipoParam = this.route.snapshot.paramMap.get('tipo');
    if (tipoParam && this.isValidTipo(tipoParam)) {
      this.selectedTipo.set(tipoParam as TipoCatalogo);
      this.loadItems();
    }
  }

  protected selectTipo(tipo: TipoCatalogo): void {
    this.selectedTipo.set(tipo);
    this.router.navigate(['/catalogos', tipo]);
    this.loadItems();
  }

  protected onTipoChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const tipo = target.value as TipoCatalogo;
    if (tipo) {
      this.selectTipo(tipo);
    }
  }

  protected onView(item: any): void {
    const tipo = this.selectedTipo();
    if (tipo) {
      this.router.navigate(['/catalogos', tipo, item.id, 'editar']);
    }
  }

  protected onEdit(item: any): void {
    const tipo = this.selectedTipo();
    if (tipo) {
      this.router.navigate(['/catalogos', tipo, item.id, 'editar']);
    }
  }

  protected onDelete(item: any): void {
    this.itemToDelete = item;
    this.deleteErrorMessage.set(null);
    this.showDeleteDialog.set(true);
  }

  protected confirmDelete(): void {
    if (!this.itemToDelete || !this.selectedTipo()) return;

    this.catalogoService.deleteByTipo(this.selectedTipo()!, this.itemToDelete.id).subscribe({
      next: () => {
        this.showDeleteDialog.set(false);
        this.itemToDelete = null;
        this.deleteErrorMessage.set(null);
        this.loadItems();
      },
      error: (err: Error) => {
        this.showDeleteDialog.set(false);
        this.itemToDelete = null;
        this.deleteErrorMessage.set(
          err.message || 'No se pudo eliminar el registro. Es posible que esté en uso por otras entidades.'
        );
      },
    });
  }

  protected cancelDelete(): void {
    this.showDeleteDialog.set(false);
    this.itemToDelete = null;
  }

  protected dismissError(): void {
    this.deleteErrorMessage.set(null);
  }

  protected createItem(): void {
    const tipo = this.selectedTipo();
    if (tipo) {
      this.router.navigate(['/catalogos', tipo, 'nuevo']);
    }
  }

  private loadItems(): void {
    const tipo = this.selectedTipo();
    if (!tipo) return;

    this.loading.set(true);
    this.catalogoService.getAllByTipo(tipo).subscribe({
      next: (data) => {
        this.items.set(data);
        this.totalItems.set(data.length);
        this.loading.set(false);
      },
      error: () => {
        this.items.set([]);
        this.totalItems.set(0);
        this.loading.set(false);
      },
    });
  }

  private isValidTipo(tipo: string): boolean {
    return TIPOS_CATALOGO.some((t) => t.tipo === tipo);
  }
}
