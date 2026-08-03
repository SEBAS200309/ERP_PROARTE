import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../../shared/components/animated-button/animated-button.component';
import { AlimentacionService } from '../alimentacion.service';

@Component({
  selector: 'app-alimentacion-ingreso-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './alimentacion-ingreso-form.component.html',
  styleUrl: './alimentacion-ingreso-form.component.scss',
})
export class AlimentacionIngresoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly alimentacionService = inject(AlimentacionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly saving = signal(false);
  protected readonly successMessage = signal<string>('');

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

    const formValue = this.form.getRawValue();
    const request = {
      cantidad: formValue.cantidad,
      descripcion: formValue.descripcion || undefined,
    };

    this.alimentacionService.registrarIngreso(this.eventoId, request).subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set('Ingreso de alimentación registrado exitosamente');
        this.form.reset();
      },
      error: () => {
        this.saving.set(false);
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
}
