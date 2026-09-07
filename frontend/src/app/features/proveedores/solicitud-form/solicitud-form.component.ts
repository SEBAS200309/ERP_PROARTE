import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { ProveedorService } from '../proveedor.service';
import {
  Proveedor,
  ServicioOption,
  EventoOption,
  CatalogoOption,
  EmpresaOption,
} from '../proveedor.models';

interface ProveedorOption {
  id: string;
  nombre: string;
}

@Component({
  selector: 'app-solicitud-form',
  standalone: true,
  imports: [ReactiveFormsModule, AnimatedButtonComponent],
  templateUrl: './solicitud-form.component.html',
  styleUrl: './solicitud-form.component.scss',
})
export class SolicitudFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly proveedorService = inject(ProveedorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly isEditMode = signal(false);
  protected readonly proveedores = signal<ProveedorOption[]>([]);
  protected readonly servicios = signal<ServicioOption[]>([]);
  protected readonly eventos = signal<EventoOption[]>([]);
  protected readonly estados = signal<CatalogoOption[]>([]);

  protected readonly pageTitle = computed(() =>
    this.isEditMode() ? 'Editar Solicitud' : 'Nueva Solicitud'
  );

  protected form!: FormGroup;
  private solicitudId: string | null = null;

  private empresasMap = new Map<string, string>();

  ngOnInit(): void {
    this.buildForm();
    this.loadCatalogos();

    this.solicitudId = this.route.snapshot.paramMap.get('id');
    if (this.solicitudId) {
      this.isEditMode.set(true);
      this.loadSolicitud(this.solicitudId);
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
      proveedorId: formValue.proveedorId,
      servicioId: formValue.servicioId,
      eventoId: formValue.eventoId || null,
      estadoId: formValue.estadoId || null,
      descripcion: formValue.descripcion || null,
    };

    if (this.isEditMode() && this.solicitudId) {
      this.proveedorService.updateSolicitud(this.solicitudId, dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/proveedores', 'solicitudes']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    } else {
      this.proveedorService.createSolicitud(dto).subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/proveedores', 'solicitudes']);
        },
        error: () => {
          this.saving.set(false);
        },
      });
    }
  }

  protected cancel(): void {
    this.router.navigate(['/proveedores', 'solicitudes']);
  }

  protected hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private buildForm(): void {
    this.form = this.fb.group({
      proveedorId: ['', [Validators.required]],
      servicioId: ['', [Validators.required]],
      eventoId: [''],
      estadoId: [''],
      descripcion: [''],
    });
  }

  private loadCatalogos(): void {
    // Carga de empresas únicamente
    this.loadProveedorEmpresas();

    this.proveedorService.getServicios().subscribe({
      next: (servicios) => this.servicios.set(servicios),
    });

    this.proveedorService.getEventos().subscribe({
      next: (eventos) => this.eventos.set(eventos),
    });

    this.proveedorService.getEstados('solicitud').subscribe({
      next: (estados) => this.estados.set(estados),
    });
  }

  private loadProveedorEmpresas(): void {
    this.proveedorService.getEmpresas().subscribe({
      next: (empresas) => {
        this.empresasMap.clear();
        empresas.forEach((e) => this.empresasMap.set(e.id, e.razonSocial));
        this.loadProveedores();
      },
    });
  }

  private loadProveedores(): void {
    // Filtro exclusivo por Empresas Proveedoras
    this.proveedorService.getAllEmpresas({ page: 0, size: 200 }).subscribe({
      next: (response) => {
        const options: ProveedorOption[] = response.content.map((p) => ({
          id: p.id,
          nombre: this.getProveedorNombre(p),
        }));
        this.proveedores.set(options);
      },
    });
  }

  private getProveedorNombre(proveedor: Proveedor): string {
    if (proveedor.empresaId) {
      const nombre = this.empresasMap.get(proveedor.empresaId);
      return nombre ?? (proveedor.especialidad || 'Empresa Proveedora');
    }
    return proveedor.especialidad || 'Empresa Proveedora';
  }

  private loadSolicitud(id: string): void {
    this.loading.set(true);
    this.proveedorService.getSolicitudes({ page: 0, size: 1000 }).subscribe({
      next: (response) => {
        const solicitud = response.content.find((s) => s.id === id);
        if (solicitud) {
          this.form.patchValue({
            proveedorId: solicitud.proveedorId,
            servicioId: solicitud.servicioId,
            eventoId: solicitud.eventoId || '',
            estadoId: solicitud.estadoId || '',
            descripcion: solicitud.descripcion || '',
          });
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/proveedores', 'solicitudes']);
      },
    });
  }
}