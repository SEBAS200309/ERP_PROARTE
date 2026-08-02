import { Component, OnInit, inject, signal, input, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { EventoService } from '../evento.service';
import { Observacion } from '../evento.models';

@Component({
  selector: 'app-evento-observaciones',
  standalone: true,
  imports: [FormsModule, AnimatedButtonComponent],
  templateUrl: './evento-observaciones.component.html',
  styleUrl: './evento-observaciones.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventoObservacionesComponent implements OnInit {
  readonly eventoId = input.required<string>();

  private readonly eventoService = inject(EventoService);

  protected readonly loading = signal(false);
  protected readonly observaciones = signal<Observacion[]>([]);
  protected readonly editingId = signal<string | null>(null);

  protected newTexto = '';
  protected editTexto = '';

  ngOnInit(): void {
    this.loadData();
  }

  protected addObservacion(): void {
    const texto = this.newTexto.trim();
    if (!texto) return;

    this.eventoService.addObservacion(this.eventoId(), { texto }).subscribe({
      next: () => {
        this.newTexto = '';
        this.loadData();
      },
    });
  }

  protected startEdit(obs: Observacion): void {
    this.editingId.set(obs.id);
    this.editTexto = obs.texto;
  }

  protected cancelEdit(): void {
    this.editingId.set(null);
    this.editTexto = '';
  }

  protected saveEdit(observacionId: string): void {
    const texto = this.editTexto.trim();
    if (!texto) return;

    this.eventoService.updateObservacion(this.eventoId(), observacionId, { texto }).subscribe({
      next: () => {
        this.editingId.set(null);
        this.editTexto = '';
        this.loadData();
      },
    });
  }

  private loadData(): void {
    this.loading.set(true);
    this.eventoService.getObservaciones(this.eventoId()).subscribe({
      next: (observaciones) => {
        this.observaciones.set(observaciones);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
