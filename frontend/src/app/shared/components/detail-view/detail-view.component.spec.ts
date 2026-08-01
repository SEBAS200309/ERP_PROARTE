import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { By } from '@angular/platform-browser';

import { DetailViewComponent, DetailField, DetailContextSection } from './detail-view.component';
import { PermissionService } from '../../../core/services/permission.service';

// Mock PermissionService
class MockPermissionService {
  private permissions: Record<string, Record<string, boolean>> = {};

  setPermission(tabla: string, accion: string, value: boolean): void {
    if (!this.permissions[tabla]) {
      this.permissions[tabla] = {};
    }
    this.permissions[tabla][accion] = value;
  }

  hasPermission(tabla: string, accion: string): boolean {
    return this.permissions[tabla]?.[accion] ?? false;
  }
}

@Component({
  standalone: true,
  imports: [DetailViewComponent],
  template: `
    <app-detail-view
      [visible]="isVisible"
      [title]="panelTitle"
      [record]="record"
      [fields]="fields"
      [contextSections]="contextSections"
      (closed)="onClosed()"
    />
  `,
})
class TestHostComponent {
  isVisible = false;
  panelTitle = 'Detalle: Cotización COT-2024-001';
  record: Record<string, any> | null = {
    codigo: 'COT-2024-001',
    estado: 'Aprobada',
    fecha: '2024-03-15',
    total: 15500000,
    activo: true,
  };
  fields: DetailField[] = [
    { key: 'codigo', label: 'Código', type: 'text' },
    { key: 'estado', label: 'Estado', type: 'text' },
    { key: 'fecha', label: 'Fecha', type: 'date' },
    { key: 'total', label: 'Total', type: 'currency' },
    { key: 'activo', label: 'Activo', type: 'boolean' },
  ];
  contextSections: DetailContextSection[] = [
    {
      tabla: 'items_cotizacion',
      title: 'Items de Cotización',
      columns: [
        { key: 'servicio', label: 'Servicio' },
        { key: 'cantidad', label: 'Cantidad', type: 'number' },
        { key: 'precio', label: 'Precio', type: 'number' },
      ],
      data: [
        { servicio: 'Sonido', cantidad: 1, precio: 5000000 },
        { servicio: 'Iluminación', cantidad: 2, precio: 3000000 },
      ],
    },
    {
      tabla: 'personas',
      title: 'Persona (Cliente)',
      columns: [
        { key: 'nombre', label: 'Nombre' },
        { key: 'documento', label: 'Documento' },
      ],
      data: [{ nombre: 'Juan Pérez', documento: 'CC 12345' }],
    },
  ];

  closedCalled = false;

  onClosed(): void {
    this.closedCalled = true;
  }
}

describe('DetailViewComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;
  let mockPermissionService: MockPermissionService;

  beforeEach(async () => {
    mockPermissionService = new MockPermissionService();

    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: PermissionService, useValue: mockPermissionService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('should not render panel when visible is false', () => {
    host.isVisible = false;
    fixture.detectChanges();

    const overlay = fixture.debugElement.query(By.css('.detail-view-overlay'));
    expect(overlay).toBeNull();
  });

  it('should render panel when visible is true', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const overlay = fixture.debugElement.query(By.css('.detail-view-overlay'));
    expect(overlay).toBeTruthy();
  });

  it('should display the title', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const titleEl = fixture.debugElement.query(By.css('.detail-view-title'));
    expect(titleEl.nativeElement.textContent).toContain('Cotización COT-2024-001');
  });

  it('should display record fields with correct labels and values', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const labels = fixture.debugElement.queryAll(By.css('.field-label'));
    const values = fixture.debugElement.queryAll(By.css('.field-value'));

    expect(labels.length).toBe(5);
    expect(labels[0].nativeElement.textContent).toContain('Código');
    expect(values[0].nativeElement.textContent).toContain('COT-2024-001');
  });

  it('should format currency fields with $ prefix', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const values = fixture.debugElement.queryAll(By.css('.field-value'));
    const totalValue = values[3].nativeElement.textContent;
    expect(totalValue).toContain('$');
    expect(totalValue).toContain('15.500.000');
  });

  it('should format boolean fields as Sí/No', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const values = fixture.debugElement.queryAll(By.css('.field-value'));
    expect(values[4].nativeElement.textContent).toContain('Sí');
  });

  it('should display — for null field values', () => {
    host.isVisible = true;
    host.record = { codigo: null, estado: 'Test', fecha: null, total: null, activo: false };
    fixture.detectChanges();

    const values = fixture.debugElement.queryAll(By.css('.field-value'));
    expect(values[0].nativeElement.textContent).toContain('—');
  });

  it('should show context sections when user has ver_listado permission', () => {
    host.isVisible = true;
    mockPermissionService.setPermission('items_cotizacion', 'ver_listado', true);
    mockPermissionService.setPermission('personas', 'ver_listado', true);
    fixture.detectChanges();

    const sections = fixture.debugElement.queryAll(By.css('.context-section'));
    expect(sections.length).toBe(2);
  });

  it('should hide context sections when user lacks ver_listado permission', () => {
    host.isVisible = true;
    mockPermissionService.setPermission('items_cotizacion', 'ver_listado', true);
    mockPermissionService.setPermission('personas', 'ver_listado', false);
    fixture.detectChanges();

    const sections = fixture.debugElement.queryAll(By.css('.context-section'));
    expect(sections.length).toBe(1);

    const title = fixture.debugElement.query(By.css('.context-title'));
    expect(title.nativeElement.textContent).toContain('Items de Cotización');
  });

  it('should hide all context sections when user has no permissions', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const sections = fixture.debugElement.queryAll(By.css('.context-section'));
    expect(sections.length).toBe(0);
  });

  it('should render context table with correct data', () => {
    host.isVisible = true;
    mockPermissionService.setPermission('items_cotizacion', 'ver_listado', true);
    fixture.detectChanges();

    const tableHeaders = fixture.debugElement.queryAll(By.css('.context-table th'));
    expect(tableHeaders.length).toBe(3);
    expect(tableHeaders[0].nativeElement.textContent).toContain('Servicio');

    const rows = fixture.debugElement.queryAll(By.css('.context-table tbody tr'));
    expect(rows.length).toBe(2);
  });

  it('should show empty message for context section with no data', () => {
    host.isVisible = true;
    host.contextSections = [
      {
        tabla: 'items_cotizacion',
        title: 'Items de Cotización',
        columns: [{ key: 'servicio', label: 'Servicio' }],
        data: [],
      },
    ];
    mockPermissionService.setPermission('items_cotizacion', 'ver_listado', true);
    fixture.detectChanges();

    const emptyMsg = fixture.debugElement.query(By.css('.context-empty'));
    expect(emptyMsg).toBeTruthy();
    expect(emptyMsg.nativeElement.textContent).toContain('Sin registros asociados');
  });

  it('should emit closed when close button is clicked', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const closeBtn = fixture.debugElement.query(By.css('.btn-close'));
    closeBtn.nativeElement.click();

    expect(host.closedCalled).toBe(true);
  });

  it('should emit closed when overlay is clicked', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const overlay = fixture.debugElement.query(By.css('.detail-view-overlay'));
    overlay.nativeElement.click();

    expect(host.closedCalled).toBe(true);
  });

  it('should NOT emit closed when panel is clicked', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const panel = fixture.debugElement.query(By.css('.detail-view-panel'));
    panel.nativeElement.click();

    expect(host.closedCalled).toBe(false);
  });

  it('should emit closed on Escape key', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const event = new KeyboardEvent('keydown', { key: 'Escape' });
    document.dispatchEvent(event);

    expect(host.closedCalled).toBe(true);
  });

  it('should have proper ARIA attributes on overlay', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const overlay = fixture.debugElement.query(By.css('.detail-view-overlay'));
    expect(overlay.attributes['role']).toBe('dialog');
    expect(overlay.attributes['aria-modal']).toBe('true');
  });

  it('should not render fields section when record is null', () => {
    host.isVisible = true;
    host.record = null;
    fixture.detectChanges();

    const fieldsSection = fixture.debugElement.query(By.css('.detail-view-fields'));
    expect(fieldsSection).toBeNull();
  });
});
