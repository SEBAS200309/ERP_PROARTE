import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { MensajeService } from '../mensaje.service';
import { Mensaje } from '../mensaje.models';

@Component({
  selector: 'app-mensaje-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './mensaje-form.component.html',
  styleUrl: './mensaje-form.component.scss',
})
export class MensajeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly mensajeService = inject(MensajeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly isEditMode = signal(false);
  protected readonly mensaje = signal<Mensaje | null>(null);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Mensaje' : 'Nuevo Mensaje'
  );

  protected form!: FormGroup;
  private mensajeId: string | null = null;

  ngOnInit(): void {
    this.buildForm();

    this.mensajeId = this.route.snapshot.paramMap.get('id');
    if (this.mensajeId) {
      this.isEditMode.set(true);
      this.loadMensaje(this.mensajeId);
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.form.getRawValue();

    if (this.isEditMode() && this.mensajeId) {
      const updateDto = {
        nombre: formValue.nombre,
        contenido: formValue.contenido || null,
      };
      this.mensajeService.update(this.mensajeId, updateDto as any).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/mensajes']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      const createDto = {
        nombre: formValue.nombre,
        contenido: formValue.contenido || undefined,
      };
      this.mensajeService.create(createDto as any).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/mensajes']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/mensajes']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      contenido: [''],
    });
  }

  private loadMensaje(id: string): void {
    this.loading.set(true);
    this.mensajeService.getById(id).subscribe({
      next: (mensaje) => {
        this.mensaje.set(mensaje);
        this.form.patchValue({
          nombre: mensaje.nombre || '',
          contenido: mensaje.contenido || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/mensajes']);
      },
    });
  }
}
