import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { UsuarioService } from '../usuario.service';
import { Rol } from '../usuario.models';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './usuario-form.component.html',
  styleUrl: './usuario-form.component.scss',
})
export class UsuarioFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly roles = signal<Rol[]>([]);
  protected readonly isEditMode = signal(false);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Usuario' : 'Nuevo Usuario'
  );

  protected form!: FormGroup;
  private usuarioId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadRoles();

    this.usuarioId = this.route.snapshot.paramMap.get('id');
    if (this.usuarioId) {
      this.isEditMode.set(true);
      this.loadUsuario(this.usuarioId);
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.form.getRawValue();

    if (this.isEditMode() && this.usuarioId) {
      const updateDto = {
        username: formValue.username,
        nombreCompleto: formValue.nombreCompleto,
        email: formValue.email,
        rolId: formValue.rolId,
        ...(formValue.password ? { password: formValue.password } : {}),
      };

      this.usuarioService.update(this.usuarioId, updateDto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/usuarios']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.usuarioService.create(formValue).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/usuarios']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/usuarios']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      nombreCompleto: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      rolId: ['', [Validators.required]],
    });
  }

  private loadRoles(): void {
    this.usuarioService.getRoles().subscribe({
      next: (roles) => this.roles.set(roles),
    });
  }

  private loadUsuario(id: string): void {
    this.loading.set(true);
    // In edit mode, password is not required
    this.form.get('password')?.clearValidators();
    this.form.get('password')?.updateValueAndValidity();

    this.usuarioService.getById(id).subscribe({
      next: (usuario) => {
        this.form.patchValue({
          username: usuario.username,
          nombreCompleto: usuario.nombreCompleto,
          email: usuario.email,
          rolId: usuario.rolId,
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/usuarios']);
      },
    });
  }
}
