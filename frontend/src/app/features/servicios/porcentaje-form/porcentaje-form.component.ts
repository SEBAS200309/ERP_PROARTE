import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PorcentajeService } from '../porcentaje.service';
import { CategoriaOption } from '../servicio.models';

@Component({
  selector: 'app-porcentaje-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './porcentaje-form.component.html',
  styleUrl: './porcentaje-form.component.scss',
})
export class PorcentajeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly porcentajeService = inject(PorcentajeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly tipos = signal<CategoriaOption[]>([]);
  protected readonly isEditMode = signal(false);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Descuento/Recargo' : 'Nuevo Descuento/Recargo'
  );

  protected form!: FormGroup;
  private porcentajeId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadTipos();

    this.porcentajeId = this.route.snapshot.paramMap.get('id');
    if (this.porcentajeId) {
      this.isEditMode.set(true);
      this.loadPorcentaje(this.porcentajeId);
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.form.getRawValue();

    const dto = {
      nombre: formValue.nombre,
      valor: Number(formValue.valor),
      tipoId: formValue.tipoId,
    };

    if (this.isEditMode() && this.porcentajeId) {
      this.porcentajeService.update(this.porcentajeId, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/descuentos-recargos']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.porcentajeService.create(dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/descuentos-recargos']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/descuentos-recargos']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      valor: [null, [Validators.required, Validators.min(0), Validators.max(100)]],
      tipoId: ['', [Validators.required]],
    });
  }

  private loadTipos(): void {
    this.porcentajeService.getTipos().subscribe({
      next: (tipos) => this.tipos.set(tipos),
    });
  }

  private loadPorcentaje(id: string): void {
    this.loading.set(true);
    this.porcentajeService.getById(id).subscribe({
      next: (porcentaje) => {
        this.form.patchValue({
          nombre: porcentaje.nombre || '',
          valor: porcentaje.valor,
          tipoId: porcentaje.tipoId || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/descuentos-recargos']);
      },
    });
  }
}
