import { Component, OnInit, inject, signal, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { CotizacionService } from '../cotizacion.service';
import { Cotizacion, EstadoOption } from '../cotizacion.models';

@Component({
  selector: 'app-cotizacion-estado',
  standalone: true,
  imports: [FormsModule, AnimatedButtonComponent],
  templateUrl: './cotizacion-estado.component.html',
  styleUrl: './cotizacion-estado.component.scss',
})
export class CotizacionEstadoComponent implements OnInit {
  private readonly cotizacionService = inject(CotizacionService);

  readonly cotizacionId = input.required<string>();
  readonly currentEstadoId = input.required<string>();
  readonly estadoChanged = output<Cotizacion>();

  protected readonly estados = signal<EstadoOption[]>([]);
  protected readonly selectedEstadoId = signal<string>('');
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string>('');

  ngOnInit(): void {
    this.loadEstados();
    this.selectedEstadoId.set(this.currentEstadoId());
  }

  protected cambiarEstado(): void {
    const nuevoEstadoId = this.selectedEstadoId();
    if (!nuevoEstadoId || nuevoEstadoId === this.currentEstadoId()) {
      this.errorMessage.set('Seleccione un estado diferente al actual');
      return;
    }

    this.errorMessage.set('');
    this.saving.set(true);

    this.cotizacionService.cambiarEstado(this.cotizacionId(), { estadoId: nuevoEstadoId }).subscribe({
      next: (cotizacion) => {
        this.saving.set(false);
        this.estadoChanged.emit(cotizacion);
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err.message || 'Error al cambiar el estado');
      },
    });
  }

  protected getEstadoNombre(estadoId: string): string {
    const estado = this.estados().find((e) => e.id === estadoId);
    return estado?.nombre || '—';
  }

  private loadEstados(): void {
    this.cotizacionService.getEstados().subscribe({
      next: (estados) => this.estados.set(estados),
    });
  }
}
