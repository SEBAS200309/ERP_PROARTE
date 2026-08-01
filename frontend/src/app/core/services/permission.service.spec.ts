import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { PermissionService } from './permission.service';
import { AuthService } from './auth.service';
import { PermisosConfig } from '../models/auth.models';

describe('PermissionService', () => {
  let service: PermissionService;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  const mockPermisos: PermisosConfig = {
    tablas: {
      lead: { ver_listado: true, ver_detalle: true, crear: true, editar: true, eliminar: false },
      persona: { ver_listado: true, ver_detalle: true, crear: false, editar: false, eliminar: false },
    },
    contexto: {
      lead: ['persona', 'empresa'],
      cotizacion: ['persona', 'empresa', 'cotizacion_item', 'servicio'],
    },
  };

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    service = TestBed.inject(PermissionService);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('hasPermission', () => {
    it('should return true when user has the permission', () => {
      service.setPermisos(mockPermisos);
      expect(service.hasPermission('lead', 'ver_listado')).toBe(true);
      expect(service.hasPermission('lead', 'crear')).toBe(true);
    });

    it('should return false when user does not have the permission', () => {
      service.setPermisos(mockPermisos);
      expect(service.hasPermission('lead', 'eliminar')).toBe(false);
      expect(service.hasPermission('persona', 'crear')).toBe(false);
    });

    it('should return false for unknown tables', () => {
      service.setPermisos(mockPermisos);
      expect(service.hasPermission('evento', 'ver_listado')).toBe(false);
    });

    it('should return false when no permissions loaded', () => {
      expect(service.hasPermission('lead', 'ver_listado')).toBe(false);
    });
  });

  describe('getContexto', () => {
    it('should return related tables for a given table', () => {
      service.setPermisos(mockPermisos);
      expect(service.getContexto('lead')).toEqual(['persona', 'empresa']);
    });

    it('should return empty array for unknown table', () => {
      service.setPermisos(mockPermisos);
      expect(service.getContexto('unknown')).toEqual([]);
    });

    it('should return empty array when no permissions loaded', () => {
      expect(service.getContexto('lead')).toEqual([]);
    });
  });

  describe('getPermisos', () => {
    it('should return the full permissions config', () => {
      service.setPermisos(mockPermisos);
      expect(service.getPermisos()).toEqual(mockPermisos);
    });

    it('should return null when no permissions loaded', () => {
      expect(service.getPermisos()).toBeNull();
    });
  });

  describe('clearPermisos', () => {
    it('should clear loaded permissions', () => {
      service.setPermisos(mockPermisos);
      expect(service.getPermisos()).not.toBeNull();

      service.clearPermisos();
      expect(service.getPermisos()).toBeNull();
    });
  });

  describe('loadPermisos', () => {
    it('should return null when no user is logged in', () => {
      let result: PermisosConfig | null | undefined;
      service.loadPermisos().subscribe((r) => (result = r));
      expect(result).toBeNull();
    });
  });
});
