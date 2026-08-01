import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Component, viewChild } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { By } from '@angular/platform-browser';

import { ConfirmDialogComponent } from './confirm-dialog.component';
import { ConfirmDialogService } from './confirm-dialog.service';

// Host component to test ConfirmDialog via inputs/outputs
@Component({
  standalone: true,
  imports: [ConfirmDialogComponent],
  template: `
    <app-confirm-dialog
      [visible]="isVisible"
      [title]="dialogTitle"
      [message]="dialogMessage"
      [confirmLabel]="confirmLabel"
      [cancelLabel]="cancelLabel"
      (confirmed)="onConfirmed()"
      (cancelled)="onCancelled()"
    />
  `,
})
class TestHostComponent {
  isVisible = false;
  dialogTitle = 'Confirmar eliminación';
  dialogMessage = '¿Está seguro que desea eliminar este registro?';
  confirmLabel = 'Eliminar';
  cancelLabel = 'Cancelar';

  confirmedCalled = false;
  cancelledCalled = false;

  onConfirmed(): void {
    this.confirmedCalled = true;
  }

  onCancelled(): void {
    this.cancelledCalled = true;
  }
}

describe('ConfirmDialogComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [provideAnimationsAsync()],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('should not render dialog when visible is false', () => {
    host.isVisible = false;
    fixture.detectChanges();

    const overlay = fixture.debugElement.query(By.css('.confirm-dialog-overlay'));
    expect(overlay).toBeNull();
  });

  it('should render dialog when visible is true', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const overlay = fixture.debugElement.query(By.css('.confirm-dialog-overlay'));
    expect(overlay).toBeTruthy();
  });

  it('should display the correct title', () => {
    host.isVisible = true;
    host.dialogTitle = 'Eliminar usuario';
    fixture.detectChanges();

    const titleEl = fixture.debugElement.query(By.css('.confirm-dialog-title'));
    expect(titleEl.nativeElement.textContent).toContain('Eliminar usuario');
  });

  it('should display the correct message', () => {
    host.isVisible = true;
    host.dialogMessage = '¿Desea continuar?';
    fixture.detectChanges();

    const messageEl = fixture.debugElement.query(By.css('.confirm-dialog-message'));
    expect(messageEl.nativeElement.textContent).toContain('¿Desea continuar?');
  });

  it('should display custom button labels', () => {
    host.isVisible = true;
    host.confirmLabel = 'Sí, eliminar';
    host.cancelLabel = 'No, volver';
    fixture.detectChanges();

    const buttons = fixture.debugElement.queryAll(By.css('.btn'));
    expect(buttons[0].nativeElement.textContent.trim()).toBe('No, volver');
    expect(buttons[1].nativeElement.textContent.trim()).toBe('Sí, eliminar');
  });

  it('should emit confirmed when Eliminar button is clicked', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const dangerBtn = fixture.debugElement.query(By.css('.btn-danger'));
    dangerBtn.nativeElement.click();

    expect(host.confirmedCalled).toBe(true);
  });

  it('should emit cancelled when Cancelar button is clicked', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const secondaryBtn = fixture.debugElement.query(By.css('.btn-secondary'));
    secondaryBtn.nativeElement.click();

    expect(host.cancelledCalled).toBe(true);
  });

  it('should emit cancelled when overlay is clicked', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const overlay = fixture.debugElement.query(By.css('.confirm-dialog-overlay'));
    overlay.nativeElement.click();

    expect(host.cancelledCalled).toBe(true);
  });

  it('should NOT emit cancelled when dialog box is clicked (event does not bubble)', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const dialogBox = fixture.debugElement.query(By.css('.confirm-dialog-box'));
    dialogBox.nativeElement.click();

    expect(host.cancelledCalled).toBe(false);
  });

  it('should emit cancelled on Escape key press', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const event = new KeyboardEvent('keydown', { key: 'Escape' });
    document.dispatchEvent(event);

    expect(host.cancelledCalled).toBe(true);
  });

  it('should have proper ARIA attributes', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const overlay = fixture.debugElement.query(By.css('.confirm-dialog-overlay'));
    expect(overlay.attributes['role']).toBe('dialog');
    expect(overlay.attributes['aria-modal']).toBe('true');
  });

  it('should display default Spanish messages', () => {
    host.isVisible = true;
    fixture.detectChanges();

    const title = fixture.debugElement.query(By.css('.confirm-dialog-title'));
    const message = fixture.debugElement.query(By.css('.confirm-dialog-message'));

    expect(title.nativeElement.textContent).toContain('Confirmar eliminación');
    expect(message.nativeElement.textContent).toContain('¿Está seguro que desea eliminar este registro?');
  });
});

describe('ConfirmDialogService', () => {
  let service: ConfirmDialogService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideAnimationsAsync(), ConfirmDialogService],
    }).compileComponents();

    service = TestBed.inject(ConfirmDialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return an Observable from confirm()', () => {
    const result$ = service.confirm();
    expect(result$).toBeTruthy();
    expect(typeof result$.subscribe).toBe('function');
  });
});
