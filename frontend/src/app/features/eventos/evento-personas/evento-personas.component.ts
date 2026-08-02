import { Component, OnInit, inject, signal, input, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { EventoService } from '../evento.service';
import { EventoContacto, PersonaOption, RolEventoOption } from '../evento.models';

@Component({
  selector: 'app-evento-personas',
  standalone: true,
  imports: [FormsModule, AnimatedButtonComponent],
  templateUrl: './evento-personas.component.html',
  styleUrl: './evento-personas.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventoPersonasComponent implements OnInit {
  readonly eventoId = input.required<string>();

  private readonly eventoService = inject(EventoService);

  protected readonly loading = signal(false);
  protected readonly contactos = signal<EventoContacto[]>([]);
  protected readonly personasOptions = signal<PersonaOption[]>([]);
  protected readonly rolesOptions = signal<RolEventoOption[]>([]);

  protected newPersonaId = '';
  protected newRolEventoId = '';
  protected newObservaciones = '';

  private personasMap = new Map<string, string>();
  private rolesMap = new Map<string, string>();

  ngOnInit(): void {
    this.loadData();
    this.loadCatalogos();
  }

  protected getPersonaNombre(personaId: string): string {
    return this.personasMap.get(personaId) || personaId;
  }

  protected getRolNombre(rolEventoId: string): string {
    return this.rolesMap.get(rolEventoId) || rolEventoId;
  }

  protected addContacto(): void {
    if (!this.newPersonaId || !this.newRolEventoId) return;

    this.eventoService.addContacto(this.eventoId(), {
      personaId: this.newPersonaId,
      rolEventoId: this.newRolEventoId,
      observaciones: this.newObservaciones || null,
    }).subscribe({
      next: () => {
        this.newPersonaId = '';
        this.newRolEventoId = '';
        this.newObservaciones = '';
        this.loadData();
      },
    });
  }

  protected removeContacto(contactoId: string): void {
    this.eventoService.removeContacto(this.eventoId(), contactoId).subscribe({
      next: () => this.loadData(),
    });
  }

  private loadData(): void {
    this.loading.set(true);
    this.eventoService.getContactos(this.eventoId()).subscribe({
      next: (contactos) => {
        this.contactos.set(contactos);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadCatalogos(): void {
    this.eventoService.getPersonas().subscribe({
      next: (options) => {
        this.personasOptions.set(options);
        this.personasMap.clear();
        options.forEach((p) => this.personasMap.set(p.id, p.nombre));
      },
    });

    this.eventoService.getRolesEvento().subscribe({
      next: (options) => {
        this.rolesOptions.set(options);
        this.rolesMap.clear();
        options.forEach((r) => this.rolesMap.set(r.id, r.nombre));
      },
    });
  }
}
