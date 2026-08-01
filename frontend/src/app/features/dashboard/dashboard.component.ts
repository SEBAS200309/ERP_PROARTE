import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { buttonAnimations } from '../../shared/animations/button.animations';
import { PermissionService } from '../../core/services/permission.service';
import { DashboardService, DashboardResumen } from './dashboard.service';

interface SummaryCard {
  icon: string;
  title: string;
  value: number;
  route: string;
}

interface QuickLink {
  icon: string;
  label: string;
  route: string;
  tabla: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [buttonAnimations],
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly permissionService = inject(PermissionService);

  protected readonly loading = signal(true);
  protected readonly error = signal(false);

  protected readonly cards = signal<SummaryCard[]>([]);

  protected readonly allLinks: QuickLink[] = [
    { icon: '🎯', label: 'Leads', route: '/leads', tabla: 'lead' },
    { icon: '👤', label: 'Personas', route: '/personas', tabla: 'persona' },
    { icon: '🏢', label: 'Empresas', route: '/empresas', tabla: 'empresa' },
    { icon: '🚚', label: 'Proveedores', route: '/proveedores', tabla: 'proveedor' },
    { icon: '🎭', label: 'Servicios', route: '/servicios', tabla: 'servicio' },
    { icon: '📋', label: 'Cotizaciones', route: '/cotizaciones', tabla: 'cotizacion' },
    { icon: '📅', label: 'Eventos', route: '/eventos', tabla: 'evento' },
    { icon: '📦', label: 'Inventario', route: '/inventario', tabla: 'insumo' },
  ];

  protected get visibleLinks(): QuickLink[] {
    return this.allLinks.filter((link) =>
      this.permissionService.hasPermission(link.tabla, 'ver_listado')
    );
  }

  // Animation state tracking
  protected hoverStates: Record<string, 'out' | 'over'> = {};

  ngOnInit(): void {
    this.loadResumen();
  }

  protected onHover(id: string, over: boolean): void {
    this.hoverStates[id] = over ? 'over' : 'out';
  }

  protected getHoverState(id: string): string {
    return this.hoverStates[id] ?? 'out';
  }

  private loadResumen(): void {
    this.loading.set(true);
    this.error.set(false);

    this.dashboardService.getResumen().subscribe({
      next: (resumen: DashboardResumen) => {
        this.cards.set([
          {
            icon: '🎯',
            title: 'Total Leads',
            value: resumen.totalLeads,
            route: '/leads',
          },
          {
            icon: '📋',
            title: 'Cotizaciones Pendientes',
            value: resumen.cotizacionesPendientes,
            route: '/cotizaciones',
          },
          {
            icon: '📅',
            title: 'Eventos Próximos',
            value: resumen.eventosProximos,
            route: '/eventos',
          },
        ]);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  protected retry(): void {
    this.loadResumen();
  }
}
