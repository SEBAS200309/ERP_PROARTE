import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { OrdenCompraService } from '../orden-compra.service';
import { OrdenCompra, EstadoOption, SolicitudOption } from '../orden-compra.models';

@Component({
  selector: 'app-orden-compra-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './orden-compra-form.component.html',
  styleUrl: './orden-compra-form.component.scss',
})
export class OrdenCompraFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly ordenCompraService = inject(OrdenCompraService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly estados = signal<EstadoOption[]>([]);
  protected readonly solicitudes = signal<SolicitudOption[]>([]);
  protected readonly isEditMode = signal(false);
  protected readonly orden = signal<OrdenCompra | null>(null);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Orden de Compra' : 'Nueva Orden de Compra'
  );

  protected form!: FormGroup;
  private ordenId: string | null = null;

  ngOnInit(): void {
    this.buildForm();
    this.loadCatalogos();

    this.ordenId = this.route.snapshot.paramMap.get('id');
    if (this.ordenId) {
      this.isEditMode.set(true);
      this.loadOrden(this.ordenId);
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.form.getRawValue();

    if (this.isEditMode() && this.ordenId) {
      const updateDto = {
        solicitudId: formValue.solicitudId || null,
        descripcion: formValue.descripcion || null,
        monto: formValue.monto != null ? Number(formValue.monto) : null,
        estadoId: formValue.estadoId || null,
      };
      this.ordenCompraService.update(this.ordenId, updateDto as any).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/ordenes-compra']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      const createDto = {
        codigo: formValue.codigo || undefined,
        solicitudId: formValue.solicitudId,
        descripcion: formValue.descripcion || undefined,
        monto: formValue.monto != null ? Number(formValue.monto) : undefined,
        estadoId: formValue.estadoId,
      };
      this.ordenCompraService.create(createDto as any).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/ordenes-compra']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/ordenes-compra']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      codigo: [''],
      solicitudId: ['', [Validators.required]],
      descripcion: [''],
      monto: [null],
      estadoId: ['', [Validators.required]],
    });
  }

  private loadCatalogos(): void {
    this.ordenCompraService.getEstados().subscribe({
      next: (estados) => this.estados.set(estados),
    });
    this.ordenCompraService.getSolicitudes().subscribe({
      next: (solicitudes) => this.solicitudes.set(solicitudes),
    });
  }

  private loadOrden(id: string): void {
    this.loading.set(true);
    this.ordenCompraService.getById(id).subscribe({
      next: (orden) => {
        this.orden.set(orden);
        this.form.patchValue({
          codigo: orden.codigo || '',
          solicitudId: orden.solicitudId || '',
          descripcion: orden.descripcion || '',
          monto: orden.monto ?? null,
          estadoId: orden.estadoId || '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/ordenes-compra']);
      },
    });
  }
}
