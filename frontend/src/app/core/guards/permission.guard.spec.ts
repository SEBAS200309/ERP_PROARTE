import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { permissionGuard } from './permission.guard';
import { PermissionService } from '../services/permission.service';
import { PermisosConfig } from '../models/auth.models';

describe('permissionGuard', () => {
  let permissionService: PermissionService;

  const mockPermisos: PermisosConfig = {
    tablas: {
      lead: { leer: true, crear: true, editar: true, eliminar: false },
    }
  };

  const mockState = { url: '/leads' } as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideRouter([]),
      ],
    });

    permissionService = TestBed.inject(PermissionService);
    permissionService.setPermisos(mockPermisos);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should allow access when user has the required permission', () => {
    const route = { data: { tabla: 'lead', accion: 'ver_listado' } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() => permissionGuard(route, mockState));

    expect(result).toBe(true);
  });

  it('should deny access when user does not have the required permission', () => {
    const route = { data: { tabla: 'lead', accion: 'eliminar' } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() => permissionGuard(route, mockState));

    expect(result).toBe(false);
  });

  it('should deny access when tabla is missing from route data', () => {
    const route = { data: { accion: 'ver_listado' } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() => permissionGuard(route, mockState));

    expect(result).toBe(false);
  });

  it('should deny access when accion is missing from route data', () => {
    const route = { data: { tabla: 'lead' } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() => permissionGuard(route, mockState));

    expect(result).toBe(false);
  });

  it('should deny access for unknown tables', () => {
    const route = { data: { tabla: 'unknown', accion: 'ver_listado' } } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() => permissionGuard(route, mockState));

    expect(result).toBe(false);
  });
});
