import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { ProveedorService } from '../proveedor.service';
import { PersonaOption, EmpresaOption } from '../proveedor.models';

@Component({
  selector: 'app-proveedor-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './proveedor-form.component.html',
  styleUrl: './proveedor-form.component.scss',
})
export class ProveedorFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly proveedorService = inject(ProveedorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly personas = signal<PersonaOption[]>([]);
  protected readonly empresas = signal<EmpresaOption[]>([]);
  protected readonly isEditMode = signal(false);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Proveedor' : 'Nuevo Proveedor'
  );

  protected form!: FormGroup;
  private proveedorId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadCatalogos();

    this.proveedorId = this.route.snapshot.paramMap.get('id');
    if (this.proveedorId) {
      this.isEditMode.set(true);
      this.loadProveedor(this.proveedorId);
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
      personaId: formValue.personaId || null,
      empresaId: formValue.empresaId || null,
      especialidad: formValue.especialidad || null,
    };

    if (this.isEditMode() && this.proveedorId) {
      this.proveedorService.update(this.proveedorId, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/proveedores']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.proveedorService.create(dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/proveedores']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/proveedores']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      personaId: [''],
      empresaId: [''],
      especialidad: ['', [Validators.maxLength(100)]],
    });
  }

  private loadCatalogos(): void {
    this.proveedorService.getPersonas().subscribe({
      next: (personas) => this.personas.set(personas),
    });
    this.proveedorService.getEmpresas().subscribe({
      next: (empresas) => this.empresas.set(empresas),
    });
  }

  private loadProveedor(id: string): void {
    this.loading.set(true);
    this.proveedorService.getById(id).subscribe({
      next: (proveedor) => {
        this.form.patchValue({
          personaId: proveedor.personaId || '',
          empresaId: proveedor.empresaId || '',
          especialidad: proveedor.especialidad || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/proveedores']);
      },
    });
  }
}
