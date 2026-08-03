import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { CatalogoService } from '../catalogo.service';
import { CatalogoItem, TipoCatalogo, TIPOS_CATALOGO } from '../catalogo.models';

@Component({
  selector: 'app-catalogo-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './catalogo-form.component.html',
  styleUrl: './catalogo-form.component.scss',
})
export class CatalogoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly catalogoService = inject(CatalogoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly isEditMode = signal(false);
  protected readonly catalogo = signal<CatalogoItem | null>(null);
  protected readonly tipo = signal<TipoCatalogo | null>(null);

  protected readonly pageTitle = computed(() => {
    const tipoInfo = TIPOS_CATALOGO.find((t) => t.tipo === this.tipo());
    const tipoLabel = tipoInfo ? tipoInfo.label : 'Catálogo';
    return this.isEditMode() ? `Editar ${tipoLabel}` : `Nuevo ${tipoLabel}`;
  });

  protected readonly showContexto = computed(() => this.tipo() === 'estado');
  protected readonly showAbreviatura = computed(() => this.tipo() === 'unidad-medida');

  protected form!: FormGroup;
  private catalogoId: string | null = null;

  ngOnInit(): void {
    const tipoParam = this.route.snapshot.paramMap.get('tipo') as TipoCatalogo;
    this.tipo.set(tipoParam);

    this.buildForm();

    this.catalogoId = this.route.snapshot.paramMap.get('id');
    if (this.catalogoId) {
      this.isEditMode.set(true);
      this.loadCatalogo(tipoParam, this.catalogoId);
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const tipo = this.tipo();
    if (!tipo) return;

    this.saving.set(true);
    const formValue = this.form.getRawValue();

    const dto: any = { nombre: formValue.nombre };
    if (tipo === 'estado' && formValue.contexto) {
      dto.contexto = formValue.contexto;
    }
    if (tipo === 'unidad-medida' && formValue.abreviatura) {
      dto.abreviatura = formValue.abreviatura;
    }

    if (this.isEditMode() && this.catalogoId) {
      this.catalogoService.updateByTipo(tipo, this.catalogoId, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/catalogos', tipo]);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.catalogoService.createByTipo(tipo, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/catalogos', tipo]);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    const tipo = this.tipo();
    this.router.navigate(['/catalogos', tipo || '']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      contexto: [''],
      abreviatura: ['', [Validators.maxLength(10)]],
    });

    // Si es estado, hacer contexto requerido
    if (this.tipo() === 'estado') {
      this.form.get('contexto')?.setValidators([Validators.required, Validators.maxLength(50)]);
    }
  }

  private loadCatalogo(tipo: TipoCatalogo, id: string): void {
    this.loading.set(true);
    this.catalogoService.getByIdAndTipo(tipo, id).subscribe({
      next: (catalogo) => {
        this.catalogo.set(catalogo);
        this.form.patchValue({
          nombre: catalogo.nombre || '',
          contexto: catalogo.contexto || '',
          abreviatura: catalogo.abreviatura || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/catalogos', tipo]);
      },
    });
  }
}
