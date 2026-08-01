import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';

import { DetailViewComponent, DetailField, DetailContextSection } from '../../../shared/components/detail-view/detail-view.component';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { PersonaService } from '../persona.service';
import { Persona, CatalogoOption } from '../persona.models';

@Component({
  selector: 'app-persona-detail',
  standalone: true,
  imports: [CommonModule, AnimatedButtonComponent],
  templateUrl: './persona-detail.component.html',
  styleUrl: './persona-detail.component.scss',
})
export class PersonaDetailComponent implements OnInit {
  private readonly personaService = inject(PersonaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(true);
  protected readonly persona = signal<Persona | null>(null);
  protected readonly tiposDocumento = signal<CatalogoOption[]>([]);
  protected readonly rolesEntidad = signal<CatalogoOption[]>([]);

  private personaId: string | null = null;

  ngOnInit(): void {
    this.personaId = this.route.snapshot.paramMap.get('id');
    if (this.personaId) {
      this.loadCatalogos();
      this.loadPersona(this.personaId);
    } else {
      this.router.navigate(['/personas']);
    }
  }

  protected getTipoDocumentoNombre(): string {
    const persona = this.persona();
    if (!persona?.tipoDocumentoId) return '—';
    const tipo = this.tiposDocumento().find((t) => t.id === persona.tipoDocumentoId);
    return tipo?.nombre || '—';
  }

  protected getRolEntidadNombre(): string {
    const persona = this.persona();
    if (!persona?.rolEntidadId) return '—';
    const rol = this.rolesEntidad().find((r) => r.id === persona.rolEntidadId);
    return rol?.nombre || '—';
  }

  protected goBack(): void {
    this.router.navigate(['/personas']);
  }

  private loadCatalogos(): void {
    this.personaService.getTiposDocumento().subscribe({
      next: (tipos) => this.tiposDocumento.set(tipos),
    });
    this.personaService.getRolesEntidad().subscribe({
      next: (roles) => this.rolesEntidad.set(roles),
    });
  }

  private loadPersona(id: string): void {
    this.loading.set(true);
    this.personaService.getById(id).subscribe({
      next: (persona) => {
        this.persona.set(persona);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/personas']);
      },
    });
  }
}
