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
      leads: { leer: true, crear: true, editar: true, eliminar: false },
      personas: { leer: true, crear: false, editar: false, eliminar: false },
    }
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
      expect(service.hasPermission('leads', 'leer')).toBe(true);
      expect(service.hasPermission('leads', 'crear')).toBe(true);
    });

    it('should return false when user does not have the permission', () => {
      service.setPermisos(mockPermisos);
      expect(service.hasPermission('leads', 'eliminar')).toBe(false);
      expect(service.hasPermission('personas', 'crear')).toBe(false);
    });

    it('should return false for unknown tables', () => {
      service.setPermisos(mockPermisos);
      expect(service.hasPermission('eventos', 'leer')).toBe(false);
    });

    it('should return false when no permissions loaded', () => {
      expect(service.hasPermission('leads', 'leer')).toBe(false);
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
