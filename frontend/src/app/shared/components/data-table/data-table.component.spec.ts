import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, signal } from '@angular/core';

import {
  DataTableComponent,
  DataTableColumn,
  DataTablePermissions,
} from './data-table.component';

// Test host component to pass signals to DataTableComponent
@Component({
  standalone: true,
  imports: [DataTableComponent],
  template: `
    <app-data-table
      [columns]="columns"
      [data]="data"
      [permissions]="permissions"
      [loading]="loading"
      [totalItems]="totalItems"
      [pageSize]="pageSize"
      [currentPage]="currentPage"
      [emptyMessage]="emptyMessage"
      (viewAction)="onView($event)"
      (editAction)="onEdit($event)"
      (deleteAction)="onDelete($event)"
      (pageChange)="onPageChange($event)"
      (sortChange)="onSortChange($event)"
      (searchChange)="onSearchChange($event)"
    />
  `,
})
class TestHostComponent {
  columns: DataTableColumn[] = [
    { key: 'nombre', label: 'Nombre', sortable: true },
    { key: 'email', label: 'Correo' },
    { key: 'fecha', label: 'Fecha', sortable: true, type: 'date' },
  ];
  data: any[] = [
    { nombre: 'Juan', email: 'juan@test.com', fecha: '2024-01-15' },
    { nombre: 'María', email: 'maria@test.com', fecha: '2024-02-20' },
  ];
  permissions: DataTablePermissions = {
    ver_detalle: true,
    editar: true,
    eliminar: true,
  };
  loading = false;
  totalItems = 20;
  pageSize = 10;
  currentPage = 0;
  emptyMessage = 'No se encontraron registros';

  viewedRow: any = null;
  editedRow: any = null;
  deletedRow: any = null;
  lastPage: number | null = null;
  lastSort: any = null;
  lastSearch: string | null = null;

  onView(row: any) { this.viewedRow = row; }
  onEdit(row: any) { this.editedRow = row; }
  onDelete(row: any) { this.deletedRow = row; }
  onPageChange(page: number) { this.lastPage = page; }
  onSortChange(event: any) { this.lastSort = event; }
  onSearchChange(term: string) { this.lastSearch = term; }
}

describe('DataTableComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    element = fixture.nativeElement;
    fixture.detectChanges();
  });

  describe('rendering', () => {
    it('should render column headers', () => {
      const headers = element.querySelectorAll('thead th');
      expect(headers.length).toBe(4); // 3 columns + 1 actions
      expect(headers[0].textContent).toContain('Nombre');
      expect(headers[1].textContent).toContain('Correo');
      expect(headers[2].textContent).toContain('Fecha');
      expect(headers[3].textContent).toContain('Acciones');
    });

    it('should render data rows', () => {
      const rows = element.querySelectorAll('tbody tr');
      expect(rows.length).toBe(2);
      expect(rows[0].textContent).toContain('Juan');
      expect(rows[1].textContent).toContain('María');
    });

    it('should render search input', () => {
      const input = element.querySelector('.search-input') as HTMLInputElement;
      expect(input).toBeTruthy();
      expect(input.placeholder).toBe('Buscar...');
    });
  });

  describe('empty state', () => {
    it('should show empty message when data is empty', () => {
      host.data = [];
      fixture.detectChanges();
      const emptyState = element.querySelector('.empty-message');
      expect(emptyState).toBeTruthy();
      expect(emptyState!.textContent).toContain('No se encontraron registros');
    });

    it('should show custom empty message', () => {
      host.data = [];
      host.emptyMessage = 'Sin resultados personalizados';
      fixture.detectChanges();
      const emptyState = element.querySelector('.empty-message');
      expect(emptyState!.textContent).toContain('Sin resultados personalizados');
    });
  });

  describe('loading state', () => {
    it('should show skeleton rows when loading', () => {
      host.loading = true;
      fixture.detectChanges();
      const skeletonRows = element.querySelectorAll('.skeleton-row');
      expect(skeletonRows.length).toBe(10); // pageSize
    });

    it('should not show data rows when loading', () => {
      host.loading = true;
      fixture.detectChanges();
      const text = element.querySelector('tbody')!.textContent;
      expect(text).not.toContain('Juan');
    });
  });

  describe('action buttons', () => {
    it('should render all action buttons when permissions are granted', () => {
      const actionBtns = element.querySelectorAll('.action-btn');
      // 2 rows × 3 buttons = 6
      expect(actionBtns.length).toBe(6);
    });

    it('should not render view button when ver_detalle is false', () => {
      host.permissions = { ver_detalle: false, editar: true, eliminar: true };
      fixture.detectChanges();
      const viewBtns = element.querySelectorAll('.action-view');
      expect(viewBtns.length).toBe(0);
    });

    it('should not render edit button when editar is false', () => {
      host.permissions = { ver_detalle: true, editar: false, eliminar: true };
      fixture.detectChanges();
      const editBtns = element.querySelectorAll('.action-edit');
      expect(editBtns.length).toBe(0);
    });

    it('should not render delete button when eliminar is false', () => {
      host.permissions = { ver_detalle: true, editar: true, eliminar: false };
      fixture.detectChanges();
      const deleteBtns = element.querySelectorAll('.action-delete');
      expect(deleteBtns.length).toBe(0);
    });

    it('should not render actions column when no permissions', () => {
      host.permissions = {};
      fixture.detectChanges();
      const headers = element.querySelectorAll('thead th');
      expect(headers.length).toBe(3); // no actions column
    });

    it('should emit viewAction when view button clicked', () => {
      const btn = element.querySelector('.action-view') as HTMLButtonElement;
      btn.click();
      fixture.detectChanges();
      expect(host.viewedRow).toEqual(host.data[0]);
    });

    it('should emit editAction when edit button clicked', () => {
      const btn = element.querySelector('.action-edit') as HTMLButtonElement;
      btn.click();
      fixture.detectChanges();
      expect(host.editedRow).toEqual(host.data[0]);
    });

    it('should emit deleteAction when delete button clicked', () => {
      const btn = element.querySelector('.action-delete') as HTMLButtonElement;
      btn.click();
      fixture.detectChanges();
      expect(host.deletedRow).toEqual(host.data[0]);
    });
  });

  describe('pagination', () => {
    it('should show page indicator', () => {
      const indicator = element.querySelector('.page-indicator');
      expect(indicator!.textContent).toContain('1 de 2');
    });

    it('should disable previous button on first page', () => {
      const prevBtn = element.querySelector('.pagination-btn') as HTMLButtonElement;
      expect(prevBtn.disabled).toBe(true);
    });

    it('should enable next button when more pages exist', () => {
      const btns = element.querySelectorAll('.pagination-btn');
      const nextBtn = btns[1] as HTMLButtonElement;
      expect(nextBtn.disabled).toBe(false);
    });

    it('should emit pageChange when next clicked', () => {
      const btns = element.querySelectorAll('.pagination-btn');
      const nextBtn = btns[1] as HTMLButtonElement;
      nextBtn.click();
      fixture.detectChanges();
      expect(host.lastPage).toBe(1);
    });

    it('should disable next button on last page', () => {
      host.currentPage = 1;
      fixture.detectChanges();
      const btns = element.querySelectorAll('.pagination-btn');
      const nextBtn = btns[1] as HTMLButtonElement;
      expect(nextBtn.disabled).toBe(true);
    });

    it('should not show pagination when loading', () => {
      host.loading = true;
      fixture.detectChanges();
      const pagination = element.querySelector('.pagination');
      expect(pagination).toBeFalsy();
    });
  });

  describe('sorting', () => {
    it('should show sort indicators on sortable columns', () => {
      const sortIcons = element.querySelectorAll('.sort-icon');
      expect(sortIcons.length).toBe(2); // nombre and fecha are sortable
    });

    it('should emit sortChange when sortable header clicked', () => {
      const headers = element.querySelectorAll('thead th');
      (headers[0] as HTMLElement).click();
      fixture.detectChanges();
      expect(host.lastSort).toEqual({ column: 'nombre', direction: 'asc' });
    });

    it('should toggle direction on second click', () => {
      const headers = element.querySelectorAll('thead th');
      (headers[0] as HTMLElement).click();
      fixture.detectChanges();
      (headers[0] as HTMLElement).click();
      fixture.detectChanges();
      expect(host.lastSort).toEqual({ column: 'nombre', direction: 'desc' });
    });

    it('should not emit sort for non-sortable column', () => {
      const headers = element.querySelectorAll('thead th');
      (headers[1] as HTMLElement).click(); // email - not sortable
      fixture.detectChanges();
      expect(host.lastSort).toBeNull();
    });
  });

  describe('search', () => {
    it('should emit searchChange on input', () => {
      const input = element.querySelector('.search-input') as HTMLInputElement;
      input.value = 'test';
      input.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      expect(host.lastSearch).toBe('test');
    });

    it('should show clear button when search has value', () => {
      const input = element.querySelector('.search-input') as HTMLInputElement;
      input.value = 'test';
      input.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      const clearBtn = element.querySelector('.search-clear');
      expect(clearBtn).toBeTruthy();
    });
  });

  describe('cell formatting', () => {
    it('should format boolean values as Sí/No', () => {
      host.columns = [{ key: 'activo', label: 'Activo', type: 'boolean' }];
      host.data = [{ activo: true }, { activo: false }];
      host.permissions = {};
      fixture.detectChanges();
      const rows = element.querySelectorAll('tbody tr');
      expect(rows[0].querySelector('td')!.textContent).toContain('Sí');
      expect(rows[1].querySelector('td')!.textContent).toContain('No');
    });

    it('should show dash for null values', () => {
      host.columns = [{ key: 'nombre', label: 'Nombre' }];
      host.data = [{ nombre: null }];
      host.permissions = {};
      fixture.detectChanges();
      const cell = element.querySelector('tbody td');
      expect(cell!.textContent).toContain('—');
    });
  });
});
