import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PresentacionService } from '../presentacion.service';
import { Presentacion, ServicioOption } from '../presentacion.models';

@Component({
  selector: 'app-presentacion-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './presentacion-form.component.html',
  styleUrl: './presentacion-form.component.scss',
})
export class PresentacionFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly presentacionService = inject(PresentacionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly downloadingPdf = signal(false);
  protected readonly isEditMode = signal(false);
  protected readonly presentacion = signal<Presentacion | null>(null);
  protected readonly servicios = signal<ServicioOption[]>([]);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Presentación' : 'Nueva Presentación'
  );

  protected form!: FormGroup;
  private presentacionId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadServicios();

    this.presentacionId = this.route.snapshot.paramMap.get('id');
    if (this.presentacionId) {
      this.isEditMode.set(true);
      this.loadPresentacion(this.presentacionId);
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.form.getRawValue();

    if (this.isEditMode() && this.presentacionId) {
      const updateDto = {
        titulo: formValue.titulo,
        descripcion: formValue.descripcion || null,
        servicioId: formValue.servicioId || null,
        contenido: formValue.contenido || null,
      };
      this.presentacionService.update(this.presentacionId, updateDto as any).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/presentaciones']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      const createDto = {
        titulo: formValue.titulo,
        descripcion: formValue.descripcion || undefined,
        servicioId: formValue.servicioId || undefined,
        contenido: formValue.contenido || undefined,
      };
      this.presentacionService.create(createDto as any).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/presentaciones']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/presentaciones']);
  }

  protected descargarPdf(): void {
    if (!this.presentacionId) return;

    this.downloadingPdf.set(true);
    this.presentacionService.descargarPdf(this.presentacionId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `presentacion-${this.presentacionId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.downloadingPdf.set(false);
      },
      error: () => {
        this.downloadingPdf.set(false);
      },
    });
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      titulo: ['', [Validators.required, Validators.maxLength(200)]],
      descripcion: [''],
      servicioId: [''],
      contenido: [''],
    });
  }

  private loadPresentacion(id: string): void {
    this.loading.set(true);
    this.presentacionService.getById(id).subscribe({
      next: (presentacion) => {
        this.presentacion.set(presentacion);
        this.form.patchValue({
          titulo: presentacion.titulo || '',
          descripcion: presentacion.descripcion || '',
          servicioId: presentacion.servicioId || '',
          contenido: presentacion.contenido || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/presentaciones']);
      },
    });
  }

  private loadServicios(): void {
    this.presentacionService.getServicios().subscribe({
      next: (servicios) => {
        this.servicios.set(servicios);
      },
      error: () => {
        this.servicios.set([]);
      },
    });
  }
}
