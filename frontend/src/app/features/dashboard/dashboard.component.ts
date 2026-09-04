import { Component, OnInit, inject, signal, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';

import { buttonAnimations } from '../../shared/animations/button.animations';
import { PermissionService } from '../../core/services/permission.service';
import { AuthService } from '../../core/services/auth.service';
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
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly cards = signal<SummaryCard[]>([]);

  // Estado para controlar la visibilidad del menú desplegable
  protected readonly dropdownOpen = signal(false);

  protected readonly allLinks: QuickLink[] = [
    { icon: 'cast-outline', label: 'Leads', route: '/leads', tabla: 'leads' },
    { icon: 'person-outline', label: 'Personas', route: '/personas', tabla: 'personas' },
    { icon: 'briefcase-outline', label: 'Empresas', route: '/empresas', tabla: 'empresas' },
    { icon: 'car-outline', label: 'Proveedores', route: '/proveedores', tabla: 'proveedores' },
    { icon: 'layers-outline', label: 'Servicios', route: '/servicios', tabla: 'servicios' },
    { icon: 'file-text-outline', label: 'Cotizaciones', route: '/cotizaciones', tabla: 'cotizaciones' },
    { icon: 'calendar-outline', label: 'Eventos', route: '/eventos', tabla: 'eventos' },
    { icon: 'cube-outline', label: 'Inventario', route: '/inventario', tabla: 'insumos' },
  ];

  protected get visibleLinks(): QuickLink[] {
    return this.allLinks.filter((link) =>
      this.permissionService.hasPermission(link.tabla, 'leer')
    );
  }

  protected hoverStates: Record<string, 'out' | 'over'> = {};

  ngOnInit(): void {
    this.loadResumen();

    this.permissionService.loadPermisos().subscribe(() => {
      this.cdr.markForCheck();
    });
  }

  protected onHover(id: string, over: boolean): void {
    this.hoverStates[id] = over ? 'over' : 'out';
  }

  protected getHoverState(id: string): string {
    return this.hoverStates[id] ?? 'out';
  }

  protected toggleDropdown(): void {
    this.dropdownOpen.update((val) => !val);
  }

  protected cerrarSesion(): void {
    this.authService.logout();
  }

  private loadResumen(): void {
    this.loading.set(true);
    this.error.set(false);

    this.dashboardService.getResumen().subscribe({
      next: (resumen: DashboardResumen) => {
        this.cards.set([
          {
            icon: 'pie-chart-outline',
            title: 'Total Leads',
            value: resumen.totalLeads,
            route: '/leads',
          },
          {
            icon: 'file-text-outline',
            title: 'Cotizaciones Pendientes',
            value: resumen.cotizacionesPendientes,
            route: '/cotizaciones',
          },
          {
            icon: 'calendar-outline',
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