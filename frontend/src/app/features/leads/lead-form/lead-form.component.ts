import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { LeadService } from '../lead.service';
import { EstadoCatalogo, PersonaOption, EmpresaOption } from '../lead.models';

@Component({
  selector: 'app-lead-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './lead-form.component.html',
  styleUrl: './lead-form.component.scss',
})
export class LeadFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly leadService = inject(LeadService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly estados = signal<EstadoCatalogo[]>([]);
  protected readonly personas = signal<PersonaOption[]>([]);
  protected readonly empresas = signal<EmpresaOption[]>([]);
  protected readonly isEditMode = signal(false);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Lead' : 'Nuevo Lead'
  );

  protected form!: FormGroup;
  private leadId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadEstados();
    this.loadPersonas();
    this.loadEmpresas();

    this.leadId = this.route.snapshot.paramMap.get('id');
    if (this.leadId) {
      this.isEditMode.set(true);
      this.loadLead(this.leadId);
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
      descripcion: formValue.descripcion,
      estadoId: formValue.estadoId || null,
      personaId: formValue.personaId || null,
      empresaId: formValue.empresaId || null,
    };

    if (this.isEditMode() && this.leadId) {
      this.leadService.update(this.leadId, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/leads']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.leadService.create(dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/leads']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/leads']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      descripcion: ['', [Validators.required]],
      estadoId: ['', [Validators.required]],
      personaId: [''],
      empresaId: [''],
    });
  }

  private loadEstados(): void {
    this.leadService.getEstados().subscribe({
      next: (estados) => {
        this.estados.set(estados);
        if (!this.isEditMode() && estados.length > 0 && !this.form.get('estadoId')?.value) {
          this.form.patchValue({ estadoId: estados[0].id });
        }
      },
    });
  }

  private loadPersonas(): void {
    this.leadService.getPersonas().subscribe({
      next: (personas) => this.personas.set(personas),
    });
  }

  private loadEmpresas(): void {
    this.leadService.getEmpresas().subscribe({
      next: (empresas) => this.empresas.set(empresas),
    });
  }

  private loadLead(id: string): void {
    this.loading.set(true);
    this.leadService.getById(id).subscribe({
      next: (lead) => {
        this.form.patchValue({
          descripcion: lead.descripcion,
          estadoId: lead.estadoId || '',
          personaId: lead.personaId || '',
          empresaId: lead.empresaId || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/leads']);
      },
    });
  }
}
