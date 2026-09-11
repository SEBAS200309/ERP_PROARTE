import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PermissionService } from '../../../core/services/permission.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly permissionService = inject(PermissionService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly loginForm = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  // DESPUÉS
  protected onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.loginForm.value;
    this.authService.login(username!, password!).subscribe({
      next: (success) => {
        if (!success) {
          this.loading.set(false);
          this.errorMessage.set(
            'Credenciales incorrectas. Verifique su usuario y contraseña'
          );
          return;
        }

        // Login correcto: cargar permisos ANTES de navegar al dashboard
        this.permissionService.loadPermisos().subscribe({
          next: () => {
            this.loading.set(false);
            this.router.navigate(['/dashboard']);
          },
          error: () => {
            // No bloqueamos el acceso si falla la carga de permisos,
            // pero sin ellos el usuario no verá módulos habilitados.
            this.loading.set(false);
            this.router.navigate(['/dashboard']);
          },
        });
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Error de conexión. Intente nuevamente');
      },
    });
  }
}
