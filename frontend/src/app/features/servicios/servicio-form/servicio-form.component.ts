import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { ServicioService } from '../servicio.service';
import { Servicio, CategoriaOption } from '../servicio.models';

@Component({
  selector: 'app-servicio-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './servicio-form.component.html',
  styleUrl: './servicio-form.component.scss',
})
export class ServicioFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly servicioService = inject(ServicioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly categorias = signal<CategoriaOption[]>([]);
  protected readonly serviciosPadre = signal<Servicio[]>([]);
  protected readonly isEditMode = signal(false);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Servicio' : 'Nuevo Servicio'
  );

  protected form!: FormGroup;
  private servicioId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadCatalogos();

    this.servicioId = this.route.snapshot.paramMap.get('id');
    if (this.servicioId) {
      this.isEditMode.set(true);
      this.loadServicio(this.servicioId);
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
      descripcion: formValue.descripcion || null,
      esPropio: formValue.esPropio,
      requiereOc: formValue.requiereOc,
      servicioPadreId: formValue.servicioPadreId || null,
      categoriaId: formValue.categoriaId || null,
    };

    if (this.isEditMode() && this.servicioId) {
      this.servicioService.update(this.servicioId, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/servicios']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.servicioService.create(dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/servicios']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/servicios']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      descripcion: ['', [Validators.maxLength(500)]],
      esPropio: [true],
      requiereOc: [false],
      servicioPadreId: [''],
      categoriaId: [''],
    });
  }

  private loadCatalogos(): void {
    this.servicioService.getCategorias().subscribe({
      next: (categorias) => this.categorias.set(categorias),
    });
    this.servicioService.getAllServicios().subscribe({
      next: (servicios) => {
        // Excluir el servicio actual (en edición) de la lista de padres posibles
        const filtered = this.servicioId
          ? servicios.filter((s) => s.id !== this.servicioId)
          : servicios;
        this.serviciosPadre.set(filtered);
      },
    });
  }

  private loadServicio(id: string): void {
    this.loading.set(true);
    this.servicioService.getById(id).subscribe({
      next: (servicio) => {
        this.form.patchValue({
          nombre: servicio.nombre || '',
          descripcion: servicio.descripcion || '',
          esPropio: servicio.esPropio,
          requiereOc: servicio.requiereOc,
          servicioPadreId: servicio.servicioPadreId || '',
          categoriaId: servicio.categoriaId || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/servicios']);
      },
    });
  }
}
