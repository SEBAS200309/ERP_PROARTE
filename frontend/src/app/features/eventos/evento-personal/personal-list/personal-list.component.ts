import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../../shared/components/animated-button/animated-button.component';
import { PersonalFormComponent } from '../personal-form/personal-form.component';
import { PersonalEventoService } from '../personal-evento.service';
import { EventoService } from '../../evento.service';
import {
  EventoPersonal,
  CreateEventoPersonalRequest,
  UpdateEventoPersonalRequest,
} from '../personal-evento.models';
import { ProveedorOption, ServicioOption } from '../../evento.models';

@Component({
  selector: 'app-personal-list',
  standalone: true,
  imports: [AnimatedButtonComponent, PersonalFormComponent],
  templateUrl: './personal-list.component.html',
  styleUrl: './personal-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonalListComponent implements OnInit {
  private readonly personalService = inject(PersonalEventoService);
  private readonly eventoService = inject(EventoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly personal = signal<EventoPersonal[]>([]);
  protected readonly editingPersonal = signal<EventoPersonal | null>(null);

  // Catálogos: solo Proveedores Persona y Servicios
  protected readonly proveedoresOptions = signal<ProveedorOption[]>([]);
  protected readonly serviciosOptions = signal<ServicioOption[]>([]);

  private proveedoresMap = new Map<string, string>();
  private serviciosMap = new Map<string, string>();

  private eventoId = '';

  ngOnInit(): void {
    this.eventoId = this.route.snapshot.paramMap.get('id') || '';
    if (!this.eventoId) {
      this.router.navigate(['/eventos']);
      return;
    }
    this.loadPersonal();
    this.loadCatalogos();
  }

  protected getProveedorNombre(proveedorId: string): string {
    return this.proveedoresMap.get(proveedorId) || proveedorId;
  }

  protected getServicioNombre(servicioId: string | null): string {
    if (!servicioId) return '—';
    return this.serviciosMap.get(servicioId) || servicioId;
  }

  protected onFormSubmitted(request: CreateEventoPersonalRequest | UpdateEventoPersonalRequest): void {
    const editing = this.editingPersonal();
    if (editing) {
      this.personalService.update(this.eventoId, editing.id, request as UpdateEventoPersonalRequest).subscribe({
        next: () => {
          this.editingPersonal.set(null);
          this.loadPersonal();
        },
      });
    } else {
      this.personalService.create(this.eventoId, request as CreateEventoPersonalRequest).subscribe({
        next: (created) => {
          this.loadPersonal();
          // Calcular valor turno automáticamente después de crear
          this.personalService.calcularValorTurno(this.eventoId, created.id).subscribe({
            next: () => this.loadPersonal(),
          });
        },
      });
    }
  }

  protected startEdit(item: EventoPersonal): void {
    this.editingPersonal.set(item);
  }

  protected cancelEdit(): void {
    this.editingPersonal.set(null);
  }

  protected calcularTurno(item: EventoPersonal): void {
    this.personalService.calcularValorTurno(this.eventoId, item.id).subscribe({
      next: () => this.loadPersonal(),
    });
  }

  protected removePersonal(personalId: string): void {
    this.personalService.delete(this.eventoId, personalId).subscribe({
      next: () => this.loadPersonal(),
    });
  }

  protected goBack(): void {
    this.router.navigate(['/eventos', this.eventoId]);
  }

  private loadPersonal(): void {
    this.loading.set(true);
    this.personalService.getAll(this.eventoId).subscribe({
      next: (data) => {
        this.personal.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadCatalogos(): void {
    // Carga solo Proveedores Persona (tipo=persona) para asignación de personal
    this.eventoService.getProveedoresOptions().subscribe({
      next: (options) => {
        this.proveedoresOptions.set(options);
        this.proveedoresMap.clear();
        options.forEach((p) => this.proveedoresMap.set(p.id, p.nombre));
      },
    });

    this.eventoService.getServicios().subscribe({
      next: (options) => {
        this.serviciosOptions.set(options);
        this.serviciosMap.clear();
        options.forEach((s) => this.serviciosMap.set(s.id, s.nombre));
      },
    });
  }
}
