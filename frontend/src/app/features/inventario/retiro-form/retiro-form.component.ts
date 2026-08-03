import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { InventarioService } from '../inventario.service';
import { InsumoOption } from '../inventario.models';

@Component({
  selector: 'app-retiro-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './retiro-form.component.html',
  styleUrl: './retiro-form.component.scss',
})
export class RetiroFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly inventarioService = inject(InventarioService);
  private readonly router = inject(Router);

  protected readonly saving = signal(false);
  protected readonly insumos = signal<InsumoOption[]>([]);
  protected readonly successMessage = signal<string>('');
  protected readonly errorMessage = signal<string>('');
  protected readonly selectedInsumoStock = signal<number | null>(null);

  protected form!: FormGroup;

  ngOnInit(): void {
    this.buildForm();
    this.loadInsumos();
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    const formValue = this.form.getRawValue();
    const request = {
      insumoId: formValue.insumoId,
      cantidad: formValue.cantidad,
      motivo: formValue.motivo || undefined,
    };

    this.inventarioService.registrarRetiro(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set('Retiro registrado exitosamente');
        this.form.reset();
        this.selectedInsumoStock.set(null);
        // Reload insumos to refresh stock
        this.loadInsumos();
      },
      error: (error: HttpErrorResponse | Error) => {
        this.saving.set(false);
        if (this.isStockError(error)) {
          this.errorMessage.set('No hay suficiente stock para este retiro');
        } else {
          const message = error instanceof Error ? error.message : 'Error al registrar el retiro';
          this.errorMessage.set(message);
        }
      },
    });
  }

  protected cancel(): void {
    this.router.navigate(['/inventario']);
  }

  protected onInsumoSelected(): void {
    const insumoId = this.form.get('insumoId')?.value;
    if (insumoId) {
      const insumo = this.insumos().find(i => i.id === insumoId);
      this.selectedInsumoStock.set(insumo?.stockActual ?? null);
    } else {
      this.selectedInsumoStock.set(null);
    }
    this.errorMessage.set('');
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      insumoId: ['', [Validators.required]],
      cantidad: [null, [Validators.required, Validators.min(0.01)]],
      motivo: [''],
    });
  }

  private loadInsumos(): void {
    this.inventarioService.getAllInsumos().subscribe({
      next: (insumos) => {
        this.insumos.set(insumos.map(i => ({ id: i.id, nombre: i.nombre, stockActual: i.stockActual })));
      },
      error: () => {
        this.insumos.set([]);
      },
    });
  }

  /**
   * Checks if the error is a stock insufficiency error from the backend.
   * The backend returns ERR_STOCK code when stock is insufficient.
   */
  private isStockError(error: any): boolean {
    // Check for error code in the unwrapped error
    if (error?.code === 'ERR_STOCK') {
      return true;
    }
    // Check for HttpErrorResponse with the error code in body
    if (error instanceof HttpErrorResponse) {
      const body = error.error;
      if (body?.error?.code === 'ERR_STOCK') {
        return true;
      }
      // Also check the message as fallback
      if (body?.error?.message?.toLowerCase()?.includes('stock')) {
        return true;
      }
    }
    // Check if Error message contains stock-related text
    if (error instanceof Error && error.message?.toLowerCase()?.includes('stock')) {
      return true;
    }
    return false;
  }
}
