import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { EmpresaService } from '../empresa.service';
import { CatalogoOption } from '../empresa.models';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './empresa-form.component.html',
  styleUrl: './empresa-form.component.scss',
})
export class EmpresaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empresaService = inject(EmpresaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly rolesEntidad = signal<CatalogoOption[]>([]);
  protected readonly isEditMode = signal(false);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Empresa' : 'Nueva Empresa'
  );

  protected form!: FormGroup;
  private empresaId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadRolesEntidad();

    this.empresaId = this.route.snapshot.paramMap.get('id');
    if (this.empresaId) {
      this.isEditMode.set(true);
      this.loadEmpresa(this.empresaId);
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
      razonSocial: formValue.razonSocial,
      nit: formValue.nit || null,
      direccion: formValue.direccion || null,
      telefono: formValue.telefono || null,
      email: formValue.email || null,
      rolEntidadId: formValue.rolEntidadId || null,
    };

    if (this.isEditMode() && this.empresaId) {
      this.empresaService.update(this.empresaId, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/empresas']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.empresaService.create(dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/empresas']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/empresas']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      razonSocial: ['', [Validators.required]],
      nit: [''],
      direccion: [''],
      telefono: [''],
      email: [''],
      rolEntidadId: [''],
    });
  }

  private loadRolesEntidad(): void {
    this.empresaService.getRolesEntidad().subscribe({
      next: (roles) => this.rolesEntidad.set(roles),
    });
  }

  private loadEmpresa(id: string): void {
    this.loading.set(true);
    this.empresaService.getById(id).subscribe({
      next: (empresa) => {
        this.form.patchValue({
          razonSocial: empresa.razonSocial,
          nit: empresa.nit || '',
          direccion: empresa.direccion || '',
          telefono: empresa.telefono || '',
          email: empresa.email || '',
          rolEntidadId: empresa.rolEntidadId || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/empresas']);
      },
    });
  }
}
