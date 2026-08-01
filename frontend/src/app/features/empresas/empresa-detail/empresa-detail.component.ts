import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { EmpresaService } from '../empresa.service';
import { Empresa, CatalogoOption } from '../empresa.models';

@Component({
  selector: 'app-empresa-detail',
  standalone: true,
  imports: [AnimatedButtonComponent],
  templateUrl: './empresa-detail.component.html',
  styleUrl: './empresa-detail.component.scss',
})
export class EmpresaDetailComponent implements OnInit {
  private readonly empresaService = inject(EmpresaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly empresa = signal<Empresa | null>(null);
  protected readonly rolesEntidad = signal<CatalogoOption[]>([]);

  protected readonly rolEntidadNombre = computed(() => {
    const empresa = this.empresa();
    const roles = this.rolesEntidad();
    if (!empresa?.rolEntidadId) return '—';
    const rol = roles.find((r) => r.id === empresa.rolEntidadId);
    return rol?.nombre || '—';
  });

  ngOnInit(): void {
    this.loadRolesEntidad();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadEmpresa(id);
    } else {
      this.router.navigate(['/empresas']);
    }
  }

  protected goBack(): void {
    this.router.navigate(['/empresas']);
  }

  private loadRolesEntidad(): void {
    this.empresaService.getRolesEntidad().subscribe({
      next: (roles) => this.rolesEntidad.set(roles),
    });
  }

  private loadEmpresa(id: string): void {
    this.loading.set(true);
    this.empresaService.getById(id).subscribe({
      next: (empresa) => {
        this.empresa.set(empresa);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/empresas']);
      },
    });
  }
}
