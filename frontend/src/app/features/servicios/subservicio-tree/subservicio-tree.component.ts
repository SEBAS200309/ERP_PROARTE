import { Component, OnInit, inject, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { ServicioService } from '../servicio.service';
import { Servicio, ServicioTreeNode, CategoriaOption } from '../servicio.models';

@Component({
  selector: 'app-subservicio-tree',
  standalone: true,
  imports: [AnimatedButtonComponent, NgTemplateOutlet],
  templateUrl: './subservicio-tree.component.html',
  styleUrl: './subservicio-tree.component.scss',
})
export class SubservicioTreeComponent implements OnInit {
  private readonly servicioService = inject(ServicioService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly treeNodes = signal<ServicioTreeNode[]>([]);
  private categoriasMap = new Map<string, string>();

  ngOnInit(): void {
    this.loadTree();
  }

  protected toggleNode(node: ServicioTreeNode): void {
    node.expanded = !node.expanded;
    if (node.expanded && node.children.length === 0) {
      this.loadChildren(node);
    }
  }

  protected navigateToServicio(servicio: Servicio): void {
    this.router.navigate(['/servicios', servicio.id, 'editar']);
  }

  protected goBack(): void {
    this.router.navigate(['/servicios']);
  }

  protected getCategoriaNombre(categoriaId: string | null): string {
    if (!categoriaId) return '—';
    return this.categoriasMap.get(categoriaId) || '—';
  }

  private loadTree(): void {
    this.loading.set(true);

    this.servicioService.getCategorias().subscribe({
      next: (categorias) => {
        categorias.forEach((c) => this.categoriasMap.set(c.id, c.nombre));
        this.loadRootServices();
      },
      error: () => {
        this.loadRootServices();
      },
    });
  }

  private loadRootServices(): void {
    this.servicioService.getAllServicios().subscribe({
      next: (servicios) => {
        // Los servicios raíz son aquellos sin servicioPadreId
        const rootServices = servicios.filter((s) => !s.servicioPadreId);
        const nodes: ServicioTreeNode[] = rootServices.map((s) => ({
          servicio: s,
          children: [],
          expanded: false,
        }));
        this.treeNodes.set(nodes);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  private loadChildren(node: ServicioTreeNode): void {
    this.servicioService.getSubservicios(node.servicio.id).subscribe({
      next: (children) => {
        node.children = children.map((c) => ({
          servicio: c,
          children: [],
          expanded: false,
        }));
        // Trigger change detection by re-setting signal
        this.treeNodes.set([...this.treeNodes()]);
      },
    });
  }
}
