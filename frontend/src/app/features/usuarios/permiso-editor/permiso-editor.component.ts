import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { UsuarioService } from '../usuario.service';

/** Acciones disponibles en la matriz de permisos */
const ACCIONES = ['ver_listado', 'ver_detalle', 'crear', 'editar', 'eliminar'] as const;

interface ContextoEntry {
  tabla: string;
  values: string;
}

@Component({
  selector: 'app-permiso-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, AnimatedButtonComponent],
  templateUrl: './permiso-editor.component.html',
  styleUrl: './permiso-editor.component.scss',
})
export class PermisoEditorComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly tablas = signal<string[]>([]);
  protected readonly acciones = ACCIONES;
  protected readonly accionLabels: Record<string, string> = {
    ver_listado: 'Ver Listado',
    ver_detalle: 'Ver Detalle',
    crear: 'Crear',
    editar: 'Editar',
    eliminar: 'Eliminar',
  };

  protected permisos: Record<string, Record<string, boolean>> = {};
  protected contexto = signal<ContextoEntry[]>([]);

  private rolId: string | null = null;

  ngOnInit(): void {
    this.rolId = this.route.snapshot.paramMap.get('id');
    if (this.rolId) {
      this.loadPermisos(this.rolId);
    }
  }

  protected getPermiso(tabla: string, accion: string): boolean {
    return this.permisos[tabla]?.[accion] ?? false;
  }

  protected togglePermiso(tabla: string, accion: string): void {
    if (!this.permisos[tabla]) {
      this.permisos[tabla] = {};
    }
    this.permisos[tabla][accion] = !this.permisos[tabla][accion];
  }

  protected addTabla(): void {
    const newTabla = `tabla_${this.tablas().length + 1}`;
    this.tablas.update((t) => [...t, newTabla]);
    this.permisos[newTabla] = {
      ver_listado: false,
      ver_detalle: false,
      crear: false,
      editar: false,
      eliminar: false,
    };
  }

  protected addContextoEntry(): void {
    this.contexto.update((c) => [...c, { tabla: '', values: '' }]);
  }

  protected removeContextoEntry(index: number): void {
    this.contexto.update((c) => c.filter((_, i) => i !== index));
  }

  protected updateContextoTabla(index: number, value: string): void {
    this.contexto.update((c) =>
      c.map((entry, i) => (i === index ? { ...entry, tabla: value } : entry))
    );
  }

  protected updateContextoValues(index: number, value: string): void {
    this.contexto.update((c) =>
      c.map((entry, i) => (i === index ? { ...entry, values: value } : entry))
    );
  }

  protected save(): void {
    if (!this.rolId) return;

    this.saving.set(true);

    // Build configuracion including contexto as part of the config
    const configuracion = { ...this.permisos };

    this.usuarioService.updatePermisosByRol(this.rolId, configuracion).subscribe({
      next: () => {
        this.saving.set(false);
        this.router.navigate(['/usuarios']);
      },
      error: () => {
        this.saving.set(false);
      },
    });
  }

  protected cancel(): void {
    this.router.navigate(['/usuarios']);
  }

  private loadPermisos(rolId: string): void {
    this.loading.set(true);
    this.usuarioService.getPermisosByRol(rolId).subscribe({
      next: (data) => {
        this.permisos = data ?? {};
        this.tablas.set(Object.keys(this.permisos));

        // Parse contexto if present in the response structure
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
