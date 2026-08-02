import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { EventoService } from '../evento.service';
import { Evento, EstadoOption, EventoInsumo, InsumoOption } from '../evento.models';
import { EventoProveedoresComponent } from '../evento-proveedores/evento-proveedores.component';
import { EventoPersonasComponent } from '../evento-personas/evento-personas.component';
import { EventoObservacionesComponent } from '../evento-observaciones/evento-observaciones.component';

type TabKey = 'proveedores' | 'personas' | 'observaciones' | 'insumos' | 'alimentacion';

@Component({
  selector: 'app-evento-detail',
  standalone: true,
  imports: [
    FormsModule,
    AnimatedButtonComponent,
    EventoProveedoresComponent,
    EventoPersonasComponent,
    EventoObservacionesComponent,
  ],
  templateUrl: './evento-detail.component.html',
  styleUrl: './evento-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventoDetailComponent implements OnInit {
  private readonly eventoService = inject(EventoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly evento = signal<Evento | null>(null);
  protected readonly estados = signal<EstadoOption[]>([]);
  protected readonly activeTab = signal<TabKey>('proveedores');

  // Insumos tab data
  protected readonly insumos = signal<EventoInsumo[]>([]);
  protected readonly insumosOptions = signal<InsumoOption[]>([]);
  protected readonly loadingInsumos = signal(false);
  protected newInsumoId = '';
  protected newInsumoCantidad = 1;

  // Alimentacion tab data
  protected readonly alimentacion = signal<any[]>([]);
  protected readonly loadingAlimentacion = signal(false);

  private estadosMap = new Map<string, string>();
  private insumosMap = new Map<string, string>();
  private eventoId = '';

  protected readonly tabs: { key: TabKey; label: string }[] = [
    { key: 'proveedores', label: 'Proveedores' },
    { key: 'personas', label: 'Personas' },
    { key: 'observaciones', label: 'Observaciones' },
    { key: 'insumos', label: 'Insumos' },
    { key: 'alimentacion', label: 'Alimentación' },
  ];

  ngOnInit(): void {
    this.eventoId = this.route.snapshot.paramMap.get('id') || '';
    if (!this.eventoId) {
      this.router.navigate(['/eventos']);
      return;
    }
    this.loadEvento();
    this.loadEstados();
  }

  protected getEstadoNombre(): string {
    const ev = this.evento();
    if (!ev || !ev.estadoId) return '—';
    return this.estadosMap.get(ev.estadoId) || '—';
  }

  protected setActiveTab(tab: TabKey): void {
    this.activeTab.set(tab);
    if (tab === 'insumos' && this.insumos().length === 0) {
      this.loadInsumos();
    }
    if (tab === 'alimentacion' && this.alimentacion().length === 0) {
      this.loadAlimentacion();
    }
  }

  protected goBack(): void {
    this.router.navigate(['/eventos']);
  }

  // ===================== INSUMOS =====================

  protected getInsumoNombre(insumoId: string): string {
    return this.insumosMap.get(insumoId) || insumoId;
  }

  protected addInsumo(): void {
    if (!this.newInsumoId || this.newInsumoCantidad < 1) return;

    this.eventoService.addInsumo(this.eventoId, {
      insumoId: this.newInsumoId,
      cantidad: this.newInsumoCantidad,
    }).subscribe({
      next: () => {
        this.newInsumoId = '';
        this.newInsumoCantidad = 1;
        this.loadInsumos();
      },
    });
  }

  protected removeInsumo(insumoId: string): void {
    this.eventoService.removeInsumo(this.eventoId, insumoId).subscribe({
      next: () => this.loadInsumos(),
    });
  }

  // ===================== PRIVATE =====================

  private loadEvento(): void {
    this.loading.set(true);
    this.eventoService.getById(this.eventoId).subscribe({
      next: (evento) => {
        this.evento.set(evento);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/eventos']);
      },
    });
  }

  private loadEstados(): void {
    this.eventoService.getEstados().subscribe({
      next: (estados) => {
        this.estados.set(estados);
        this.estadosMap.clear();
        estados.forEach((e) => this.estadosMap.set(e.id, e.nombre));
      },
    });
  }

  private loadInsumos(): void {
    this.loadingInsumos.set(true);
    this.eventoService.getInsumos(this.eventoId).subscribe({
      next: (insumos) => {
        this.insumos.set(insumos);
        this.loadingInsumos.set(false);
      },
      error: () => this.loadingInsumos.set(false),
    });

    if (this.insumosOptions().length === 0) {
      this.eventoService.getInsumosOptions().subscribe({
        next: (options) => {
          this.insumosOptions.set(options);
          this.insumosMap.clear();
          options.forEach((i) => this.insumosMap.set(i.id, i.nombre));
        },
      });
    }
  }

  private loadAlimentacion(): void {
    this.loadingAlimentacion.set(true);
    this.eventoService.getAlimentacion(this.eventoId).subscribe({
      next: (data) => {
        this.alimentacion.set(data);
        this.loadingAlimentacion.set(false);
      },
      error: () => this.loadingAlimentacion.set(false),
    });
  }
}
