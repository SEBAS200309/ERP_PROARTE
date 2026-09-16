import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map, of } from 'rxjs';

import { ApiResponse, PermisosConfig, TablaPermisos } from '../models/auth.models';
import { AuthService } from './auth.service';

const ROLES_API = '/api/v1/roles';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private readonly permisos = signal<PermisosConfig | null>(null);
  private loaded = false;

  readonly permisosLoaded = computed(() => this.permisos() !== null);

  loadPermisos(): Observable<PermisosConfig | null> {
    const user = this.authService.currentUser();
    if (!user || !user.rolId) return of(null);

    if (this.loaded && this.permisos()) {
      return of(this.permisos());
    }

    return this.http
      .get<ApiResponse<any>>(`${ROLES_API}/${user.rolId}`)
      .pipe(
        tap((response) => {
          if (response.success && response.data) {
            // Extraemos de forma segura la configuración sin romper tipados estrictos
            const data = response.data;
            let tablasPermisos: Record<string, TablaPermisos> = {};

            if (data.configuracion) {
              tablasPermisos = data.configuracion;
            } else if (typeof data === 'object' && !Array.isArray(data)) {
              tablasPermisos = data as Record<string, TablaPermisos>;
            }

            this.permisos.set({ tablas: tablasPermisos });
            this.loaded = true;
          }
        }),
        map((response) => {
          if (!response.success || !response.data) return null;

          const data = response.data;
          let tablasPermisos: Record<string, TablaPermisos> = {};

          if (data.configuracion) {
            tablasPermisos = data.configuracion;
          } else if (typeof data === 'object' && !Array.isArray(data)) {
            tablasPermisos = data as Record<string, TablaPermisos>;
          }

          return { tablas: tablasPermisos };
        })
      );
  }

  hasPermission(tabla: string, accion: keyof TablaPermisos): boolean {
    const config = this.permisos();
    if (!config || !config.tablas) return false;

    const tablaPermisos = config.tablas[tabla];
    if (!tablaPermisos) return false;

    return tablaPermisos[accion] === true;
  }

  getPermisos(): PermisosConfig | null {
    return this.permisos();
  }

  setPermisos(config: PermisosConfig): void {
    this.permisos.set(config);
    this.loaded = true;
  }

  clearPermisos(): void {
    this.permisos.set(null);
    this.loaded = false;
  }
}