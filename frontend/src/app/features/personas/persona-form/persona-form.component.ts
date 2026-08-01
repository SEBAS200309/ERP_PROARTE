import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PersonaService } from '../persona.service';
import { CatalogoOption } from '../persona.models';

@Component({
  selector: 'app-persona-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './persona-form.component.html',
  styleUrl: './persona-form.component.scss',
})
export class PersonaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly personaService = inject(PersonaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly tiposDocumento = signal<CatalogoOption[]>([]);
  protected readonly rolesEntidad = signal<CatalogoOption[]>([]);
  protected readonly isEditMode = signal(false);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Persona' : 'Nueva Persona'
  );

  protected form!: FormGroup;
  private personaId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadTiposDocumento();
    this.loadRolesEntidad();

    this.personaId = this.route.snapshot.paramMap.get('id');
    if (this.personaId) {
      this.isEditMode.set(true);
      this.loadPersona(this.personaId);
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
      nombres: formValue.nombres,
      apellidos: formValue.apellidos,
      tipoDocumentoId: formValue.tipoDocumentoId || null,
      documento: formValue.documento || null,
      telefono: formValue.telefono || null,
      email: formValue.email || null,
      direccion: formValue.direccion || null,
      rolEntidadId: formValue.rolEntidadId || null,
    };

    if (this.isEditMode() && this.personaId) {
      this.personaService.update(this.personaId, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/personas']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.personaService.create(dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/personas']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/personas']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nombres: ['', [Validators.required]],
      apellidos: ['', [Validators.required]],
      tipoDocumentoId: [''],
      documento: [''],
      telefono: [''],
      email: [''],
      direccion: [''],
      rolEntidadId: [''],
    });
  }

  private loadTiposDocumento(): void {
    this.personaService.getTiposDocumento().subscribe({
      next: (tipos) => this.tiposDocumento.set(tipos),
    });
  }

  private loadRolesEntidad(): void {
    this.personaService.getRolesEntidad().subscribe({
      next: (roles) => this.rolesEntidad.set(roles),
    });
  }

  private loadPersona(id: string): void {
    this.loading.set(true);
    this.personaService.getById(id).subscribe({
      next: (persona) => {
        this.form.patchValue({
          nombres: persona.nombres,
          apellidos: persona.apellidos,
          tipoDocumentoId: persona.tipoDocumentoId || '',
          documento: persona.documento || '',
          telefono: persona.telefono || '',
          email: persona.email || '',
          direccion: persona.direccion || '',
          rolEntidadId: persona.rolEntidadId || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/personas']);
      },
    });
  }
}
