import { Component, OnInit, inject, signal, input, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { EventoService } from '../evento.service';
import { EventoProveedor, ProveedorOption, ServicioOption } from '../evento.models';

@Component({
  selector: 'app-evento-proveedores',
  standalone: true,
  imports: [FormsModule, AnimatedButtonComponent],
  templateUrl: './evento-proveedores.component.html',
  styleUrl: './evento-proveedores.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EventoProveedoresComponent implements OnInit {
  readonly eventoId = input.required<string>();

  private readonly eventoService = inject(EventoService);

  protected readonly loading = signal(false);
  protected readonly proveedores = signal<EventoProveedor[]>([]);
  protected readonly proveedoresOptions = signal<ProveedorOption[]>([]);
  protected readonly serviciosOptions = signal<ServicioOption[]>([]);

  protected newProveedorId = '';
  protected newServicioId = '';

  private proveedoresMap = new Map<string, string>();
  private serviciosMap = new Map<string, string>();

  ngOnInit(): void {
    this.loadData();
    this.loadCatalogos();
  }

  protected getProveedorNombre(proveedorId: string): string {
    return this.proveedoresMap.get(proveedorId) || proveedorId;
  }

  protected getServicioNombre(servicioId: string): string {
    return this.serviciosMap.get(servicioId) || servicioId;
  }

  protected addProveedor(): void {
    if (!this.newProveedorId || !this.newServicioId) return;

    this.eventoService.addProveedor(this.eventoId(), {
      proveedorId: this.newProveedorId,
      servicioId: this.newServicioId,
    }).subscribe({
      next: () => {
        this.newProveedorId = '';
        this.newServicioId = '';
        this.loadData();
      },
    });
  }

  protected removeProveedor(proveedorId: string): void {
    this.eventoService.removeProveedor(this.eventoId(), proveedorId).subscribe({
      next: () => this.loadData(),
    });
  }

  private loadData(): void {
    this.loading.set(true);
    this.eventoService.getProveedores(this.eventoId()).subscribe({
      next: (proveedores) => {
        this.proveedores.set(proveedores);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadCatalogos(): void {
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
