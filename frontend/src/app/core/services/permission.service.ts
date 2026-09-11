import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map, of } from 'rxjs';

import { ApiResponse, PermisosConfig, TablaPermisos } from '../models/auth.models';
import { AuthService } from './auth.service';

const PERMISOS_API = '/api/v1/usuarios';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private readonly permisos = signal<PermisosConfig | null>(null);
  private loaded = false;

  readonly permisosLoaded = computed(() => this.permisos() !== null);

  loadPermisos(): Observable<PermisosConfig | null> {
    const user = this.authService.currentUser();
    if (!user) return of(null);

    if (this.loaded && this.permisos()) {
      return of(this.permisos());
    }

    return this.http
      .get<ApiResponse<Record<string, TablaPermisos>>>(`${PERMISOS_API}/roles/${user.rolId}/permisos`)
      .pipe(
        tap((response) => {
          if (response.success) {
            this.permisos.set({ tablas: response.data });
            this.loaded = true;
          }
        }),
        map((response) => (response.success ? { tablas: response.data } : null))
      );
  }

  hasPermission(tabla: string, accion: keyof TablaPermisos): boolean {
    const config = this.permisos();
    if (!config) return false;

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
