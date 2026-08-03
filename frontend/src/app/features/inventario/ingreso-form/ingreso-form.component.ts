import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { InventarioService } from '../inventario.service';
import { InsumoOption } from '../inventario.models';

@Component({
  selector: 'app-ingreso-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './ingreso-form.component.html',
  styleUrl: './ingreso-form.component.scss',
})
export class IngresoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly inventarioService = inject(InventarioService);
  private readonly router = inject(Router);

  protected readonly saving = signal(false);
  protected readonly insumos = signal<InsumoOption[]>([]);
  protected readonly successMessage = signal<string>('');

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

    const formValue = this.form.getRawValue();
    const request = {
      insumoId: formValue.insumoId,
      cantidad: formValue.cantidad,
      motivo: formValue.motivo || undefined,
    };

    this.inventarioService.registrarIngreso(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set('Ingreso registrado exitosamente');
        this.form.reset();
      },
      error: () => {
        this.saving.set(false);
      },
    });
  }

  protected cancel(): void {
    this.router.navigate(['/inventario']);
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
}
