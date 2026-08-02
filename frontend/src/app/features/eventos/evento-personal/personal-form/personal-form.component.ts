import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AnimatedButtonComponent } from '../../../../shared/components/animated-button/animated-button.component';
import { EventoPersonal, CreateEventoPersonalRequest, UpdateEventoPersonalRequest } from '../personal-evento.models';
import { PersonaOption, ProveedorOption, ServicioOption } from '../../evento.models';

@Component({
  selector: 'app-personal-form',
  standalone: true,
  imports: [FormsModule, AnimatedButtonComponent],
  templateUrl: './personal-form.component.html',
  styleUrl: './personal-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonalFormComponent {
  /** Opciones de catálogo */
  readonly personasOptions = input.required<PersonaOption[]>();
  readonly proveedoresOptions = input.required<ProveedorOption[]>();
  readonly serviciosOptions = input.required<ServicioOption[]>();

  /** Si se pasa un personal, se está editando */
  readonly editingPersonal = input<EventoPersonal | null>(null);

  /** Eventos de salida */
  readonly submitted = output<CreateEventoPersonalRequest | UpdateEventoPersonalRequest>();
  readonly cancelled = output<void>();

  // Form fields
  protected personaId = '';
  protected proveedorId = '';
  protected servicioId = '';
  protected tieneArl = false;
  protected tieneOp = false;
  protected observaciones = '';

  protected readonly isEditing = signal(false);

  ngOnChanges(): void {
    const personal = this.editingPersonal();
    if (personal) {
      this.isEditing.set(true);
      this.personaId = personal.personaId;
      this.proveedorId = personal.proveedorId;
      this.servicioId = personal.servicioId || '';
      this.tieneArl = personal.tieneArl ?? false;
      this.tieneOp = personal.tieneOp ?? false;
      this.observaciones = personal.observaciones || '';
    } else {
      this.isEditing.set(false);
      this.resetForm();
    }
  }

  protected submitForm(): void {
    if (this.isEditing()) {
      const request: UpdateEventoPersonalRequest = {
        servicioId: this.servicioId || null,
        tieneArl: this.tieneArl,
        tieneOp: this.tieneOp,
        observaciones: this.observaciones || null,
      };
      this.submitted.emit(request);
    } else {
      if (!this.personaId || !this.proveedorId) return;
      const request: CreateEventoPersonalRequest = {
        personaId: this.personaId,
        proveedorId: this.proveedorId,
        servicioId: this.servicioId || null,
        tieneArl: this.tieneArl,
        tieneOp: this.tieneOp,
        observaciones: this.observaciones || null,
      };
      this.submitted.emit(request);
    }
  }

  protected cancel(): void {
    this.resetForm();
    this.cancelled.emit();
  }

  private resetForm(): void {
    this.personaId = '';
    this.proveedorId = '';
    this.servicioId = '';
    this.tieneArl = false;
    this.tieneOp = false;
    this.observaciones = '';
  }
}
