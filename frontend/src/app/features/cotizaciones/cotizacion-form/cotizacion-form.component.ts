import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { CotizacionService } from '../cotizacion.service';
import { CotizacionItemsComponent } from '../cotizacion-items/cotizacion-items.component';
import { CotizacionEstadoComponent } from '../cotizacion-estado/cotizacion-estado.component';
import {
  Cotizacion,
  CotizacionItemRequest,
  EstadoOption,
  PersonaOption,
  EmpresaOption,
} from '../cotizacion.models';

@Component({
  selector: 'app-cotizacion-form',
  standalone: true,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    AnimatedButtonComponent,
    CotizacionItemsComponent,
    CotizacionEstadoComponent,
  ],
  templateUrl: './cotizacion-form.component.html',
  styleUrl: './cotizacion-form.component.scss',
})
export class CotizacionFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly estados = signal<EstadoOption[]>([]);
  protected readonly personas = signal<PersonaOption[]>([]);
  protected readonly empresas = signal<EmpresaOption[]>([]);
  protected readonly isEditMode = signal(false);
  protected readonly cotizacion = signal<Cotizacion | null>(null);
  protected readonly items = signal<CotizacionItemRequest[]>([]);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Cotización' : 'Nueva Cotización'
  );

  protected form!: FormGroup;
  private cotizacionId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadCatalogos();

    this.cotizacionId = this.route.snapshot.paramMap.get('id');
    if (this.cotizacionId) {
      this.isEditMode.set(true);
      this.loadCotizacion(this.cotizacionId);
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
      codigo: formValue.codigo || undefined,
      estadoId: formValue.estadoId,
      fechaVencimiento: formValue.fechaVencimiento || null,
      personaId: formValue.personaId || null,
      empresaId: formValue.empresaId || null,
      items: this.items(),
    };

    if (this.isEditMode() && this.cotizacionId) {
      this.cotizacionService.update(this.cotizacionId, dto as any).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/cotizaciones']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.cotizacionService.create(dto as any).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/cotizaciones']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/cotizaciones']);
  }

  protected onItemsChange(items: CotizacionItemRequest[]): void {
    this.items.set(items);
  }

  protected onEstadoChanged(cotizacion: Cotizacion): void {
    this.cotizacion.set(cotizacion);
    this.form.patchValue({ estadoId: cotizacion.estadoId });
  }

  protected recalcular(): void {
    if (!this.cotizacionId) return;
    this.cotizacionService.recalcularTotal(this.cotizacionId).subscribe({
      next: (updated) => {
        this.cotizacion.set(updated);
      },
    });
  }

  protected downloadPdf(): void {
    if (!this.cotizacionId) return;
    this.cotizacionService.downloadPdf(this.cotizacionId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `cotizacion-${this.cotizacion()?.codigo || this.cotizacionId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
    });
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      codigo: [''],
      estadoId: ['', [Validators.required]],
      fechaVencimiento: [''],
      personaId: [''],
      empresaId: [''],
    });
  }

  private loadCatalogos(): void {
    this.cotizacionService.getEstados().subscribe({
      next: (estados) => this.estados.set(estados),
    });
    this.cotizacionService.getPersonas().subscribe({
      next: (personas) => this.personas.set(personas),
    });
    this.cotizacionService.getEmpresas().subscribe({
      next: (empresas) => this.empresas.set(empresas),
    });
  }

  private loadCotizacion(id: string): void {
    this.loading.set(true);
    this.cotizacionService.getById(id).subscribe({
      next: (cotizacion) => {
        this.cotizacion.set(cotizacion);
        this.form.patchValue({
          codigo: cotizacion.codigo || '',
          estadoId: cotizacion.estadoId || '',
          fechaVencimiento: cotizacion.fechaVencimiento || '',
          personaId: cotizacion.personaId || '',
          empresaId: cotizacion.empresaId || '',
        });
        // Map existing items to request format
        const itemRequests: CotizacionItemRequest[] = cotizacion.items.map((item) => ({
          servicioId: item.servicioId,
          cantidad: item.cantidad,
          precioUnitario: item.precioUnitario,
          descuentoRecargoId: item.descuentoRecargoId || undefined,
        }));
        this.items.set(itemRequests);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/cotizaciones']);
      },
    });
  }
}
