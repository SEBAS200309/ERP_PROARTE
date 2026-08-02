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
  PersonaOption,
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

  private personasMap = new Map<string, string>();
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
    // Load personas & empresas first, then build proveedor display names
    this.proveedorService.getPersonas().subscribe({
      next: (personas) => {
        this.personasMap.clear();
        personas.forEach((p) => this.personasMap.set(p.id, `${p.nombres} ${p.apellidos}`));
        this.loadProveedorEmpresas();
      },
    });

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
    this.proveedorService.getAll({ page: 0, size: 200 }).subscribe({
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
    if (proveedor.personaId) {
      const nombre = this.personasMap.get(proveedor.personaId);
      return nombre ?? (proveedor.especialidad || 'Proveedor');
    }
    if (proveedor.empresaId) {
      const nombre = this.empresasMap.get(proveedor.empresaId);
      return nombre ?? (proveedor.especialidad || 'Proveedor');
    }
    return proveedor.especialidad || 'Proveedor';
  }

  private loadSolicitud(id: string): void {
    this.loading.set(true);
    // We need to get the solicitud data — use getSolicitudes with search
    // Since there's no getSolicitudById, we'll search from the list
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
