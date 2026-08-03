import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AnimatedButtonComponent } from '../../../../shared/components/animated-button/animated-button.component';
import { AlimentacionService } from '../alimentacion.service';

@Component({
  selector: 'app-alimentacion-retiro-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './alimentacion-retiro-form.component.html',
  styleUrl: './alimentacion-retiro-form.component.scss',
})
export class AlimentacionRetiroFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly alimentacionService = inject(AlimentacionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly saving = signal(false);
  protected readonly successMessage = signal<string>('');
  protected readonly errorMessage = signal<string>('');

  protected form!: FormGroup;
  protected eventoId = '';

  ngOnInit(): void {
    this.eventoId = this.route.snapshot.paramMap.get('id') ?? '';
    this.buildForm();
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
      cantidad: formValue.cantidad,
      descripcion: formValue.descripcion || undefined,
    };

    this.alimentacionService.registrarRetiro(this.eventoId, request).subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set('Retiro de alimentación registrado exitosamente');
        this.form.reset();
      },
      error: (error: HttpErrorResponse | Error) => {
        this.saving.set(false);
        if (this.isStockError(error)) {
          this.errorMessage.set('No hay suficiente cantidad para este retiro');
        } else {
          const message = error instanceof Error ? error.message : 'Error al registrar el retiro';
          this.errorMessage.set(message);
        }
      },
    });
  }

  protected cancel(): void {
    this.router.navigate(['/eventos', this.eventoId, 'alimentacion']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      cantidad: [null, [Validators.required, Validators.min(0.01)]],
      descripcion: ['', [Validators.maxLength(200)]],
    });
  }

  /**
   * Checks if the error is a stock/quantity insufficiency error from the backend.
   */
  private isStockError(error: any): boolean {
    if (error?.code === 'ERR_STOCK') {
      return true;
    }
    if (error instanceof HttpErrorResponse) {
      const body = error.error;
      if (body?.error?.code === 'ERR_STOCK') {
        return true;
      }
      if (body?.error?.message?.toLowerCase()?.includes('stock') ||
          body?.error?.message?.toLowerCase()?.includes('cantidad')) {
        return true;
      }
    }
    if (error instanceof Error) {
      if ((error as any).code === 'ERR_STOCK') {
        return true;
      }
      if (error.message?.toLowerCase()?.includes('stock') ||
          error.message?.toLowerCase()?.includes('cantidad')) {
        return true;
      }
    }
    return false;
  }
}
