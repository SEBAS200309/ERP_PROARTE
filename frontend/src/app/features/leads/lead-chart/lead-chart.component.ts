import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { LeadService } from '../lead.service';

interface PieSlice {
  nombre: string;
  cantidad: number;
  porcentaje: number;
  color: string;
  startAngle: number;
  endAngle: number;
  path: string;
}

@Component({
  selector: 'app-lead-chart',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './lead-chart.component.html',
  styleUrl: './lead-chart.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LeadChartComponent implements OnInit {
  private readonly leadService = inject(LeadService);

  protected readonly loading = signal(false);
  protected readonly estadisticas = signal<Record<string, number>>({});

  private readonly chartColors = [
    'var(--chart-color-1, #8B5CF6)',
    'var(--chart-color-2, #A855F7)',
    'var(--chart-color-3, #C084FC)',
    'var(--chart-color-4, #D8B4FE)',
    'var(--chart-color-5, #E9D5FF)',
    'var(--chart-color-6, #6B3FA0)',
    'var(--chart-color-7, #5B21B6)',
    'var(--chart-color-8, #4C1D95)',
  ];

  protected readonly slices = computed<PieSlice[]>(() => {
    const stats = this.estadisticas();
    const entries = Object.entries(stats).filter(([, count]) => count > 0);
    const total = entries.reduce((sum, [, count]) => sum + count, 0);

    if (total === 0) return [];

    let currentAngle = 0;
    return entries.map(([nombre, cantidad], index) => {
      const porcentaje = (cantidad / total) * 100;
      const sliceAngle = (cantidad / total) * 360;
      const startAngle = currentAngle;
      const endAngle = currentAngle + sliceAngle;
      currentAngle = endAngle;

      return {
        nombre,
        cantidad,
        porcentaje,
        color: this.chartColors[index % this.chartColors.length],
        startAngle,
        endAngle,
        path: this.describeArc(100, 100, 80, startAngle, endAngle),
      };
    });
  });

  protected readonly totalLeads = computed(() => {
    const stats = this.estadisticas();
    return Object.values(stats).reduce((sum, count) => sum + count, 0);
  });

  protected readonly hasData = computed(() => this.totalLeads() > 0);

  ngOnInit(): void {
    this.loadEstadisticas();
  }

  private loadEstadisticas(): void {
    this.loading.set(true);
    this.leadService.getEstadisticas().subscribe({
      next: (data) => {
        this.estadisticas.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  private describeArc(cx: number, cy: number, r: number, startAngle: number, endAngle: number): string {
    // Handle full circle case
    if (endAngle - startAngle >= 359.99) {
      return [
        `M ${cx} ${cy - r}`,
        `A ${r} ${r} 0 1 1 ${cx - 0.01} ${cy - r}`,
        'Z',
      ].join(' ');
    }

    const startRad = (startAngle - 90) * (Math.PI / 180);
    const endRad = (endAngle - 90) * (Math.PI / 180);

    const x1 = cx + r * Math.cos(startRad);
    const y1 = cy + r * Math.sin(startRad);
    const x2 = cx + r * Math.cos(endRad);
    const y2 = cy + r * Math.sin(endRad);

    const largeArcFlag = endAngle - startAngle > 180 ? 1 : 0;

    return [
      `M ${cx} ${cy}`,
      `L ${x1} ${y1}`,
      `A ${r} ${r} 0 ${largeArcFlag} 1 ${x2} ${y2}`,
      'Z',
    ].join(' ');
  }
}
