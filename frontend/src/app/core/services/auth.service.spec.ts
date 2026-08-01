import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Router } from '@angular/router';

import { AuthService } from './auth.service';

function createMockJwt(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = btoa(JSON.stringify(payload));
  const signature = 'mock-signature';
  return `${header}.${body}.${signature}`;
}

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('login', () => {
    it('should store tokens and return true on successful login', () => {
      const mockToken = createMockJwt({
        sub: 'user-123',
        username: 'admin',
        rol: 'Administrador',
        rolId: 'rol-456',
        exp: Math.floor(Date.now() / 1000) + 3600,
        iat: Math.floor(Date.now() / 1000),
      });

      let result: boolean | undefined;
      service.login('admin', 'password123').subscribe((r) => (result = r));

      const req = httpMock.expectOne('/api/v1/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ username: 'admin', password: 'password123' });

      req.flush({
        success: true,
        data: {
          accessToken: mockToken,
          refreshToken: 'refresh-token-xyz',
          usuario: { id: 'user-123', username: 'admin', rol: 'Administrador' },
        },
        message: 'Login exitoso',
      });

      expect(result).toBe(true);
      expect(localStorage.getItem('erp-proarte-access-token')).toBe(mockToken);
      expect(localStorage.getItem('erp-proarte-refresh-token')).toBe('refresh-token-xyz');
    });

    it('should return false on failed login', () => {
      let result: boolean | undefined;
      service.login('admin', 'wrong').subscribe((r) => (result = r));

      const req = httpMock.expectOne('/api/v1/auth/login');
      req.error(new ProgressEvent('error'), { status: 401 });

      expect(result).toBe(false);
    });
  });

  describe('logout', () => {
    it('should clear tokens and navigate to login', () => {
      localStorage.setItem('erp-proarte-access-token', 'some-token');
      localStorage.setItem('erp-proarte-refresh-token', 'refresh');

      const navigateSpy = vi.spyOn(router, 'navigate');

      service.logout();

      // Flush the logout HTTP request
      const req = httpMock.expectOne('/api/v1/auth/logout');
      req.flush({});

      expect(localStorage.getItem('erp-proarte-access-token')).toBeNull();
      expect(localStorage.getItem('erp-proarte-refresh-token')).toBeNull();
      expect(navigateSpy).toHaveBeenCalledWith(['/auth/login']);
    });
  });

  describe('getAccessToken', () => {
    it('should return token from localStorage', () => {
      localStorage.setItem('erp-proarte-access-token', 'my-token');
      expect(service.getAccessToken()).toBe('my-token');
    });

    it('should return null when no token', () => {
      expect(service.getAccessToken()).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when valid non-expired token exists', () => {
      const token = createMockJwt({
        sub: 'user-1',
        username: 'test',
        rol: 'Admin',
        rolId: 'r1',
        exp: Math.floor(Date.now() / 1000) + 3600,
        iat: Math.floor(Date.now() / 1000),
      });
      localStorage.setItem('erp-proarte-access-token', token);

      expect(service.isAuthenticated()).toBe(true);
    });

    it('should return false when token is expired', () => {
      const token = createMockJwt({
        sub: 'user-1',
        username: 'test',
        rol: 'Admin',
        rolId: 'r1',
        exp: Math.floor(Date.now() / 1000) - 100,
        iat: Math.floor(Date.now() / 1000) - 3700,
      });
      localStorage.setItem('erp-proarte-access-token', token);

      expect(service.isAuthenticated()).toBe(false);
    });

    it('should return false when no token', () => {
      expect(service.isAuthenticated()).toBe(false);
    });
  });

  describe('currentUser', () => {
    it('should return user data after login', () => {
      const token = createMockJwt({
        sub: 'user-123',
        username: 'admin',
        rol: 'Administrador',
        rolId: 'rol-456',
        exp: Math.floor(Date.now() / 1000) + 3600,
        iat: Math.floor(Date.now() / 1000),
      });

      service.login('admin', 'pass').subscribe();

      httpMock.expectOne('/api/v1/auth/login').flush({
        success: true,
        data: {
          accessToken: token,
          refreshToken: 'ref',
          usuario: { id: 'user-123', username: 'admin', rol: 'Administrador' },
        },
        message: '',
      });

      const user = service.currentUser();
      expect(user).toEqual({
        id: 'user-123',
        username: 'admin',
        rol: 'Administrador',
        rolId: 'rol-456',
      });
    });

    it('should return null when no token', () => {
      expect(service.currentUser()).toBeNull();
    });
  });

  describe('refreshToken', () => {
    it('should update access token on refresh', () => {
      localStorage.setItem('erp-proarte-refresh-token', 'old-refresh');

      const newToken = createMockJwt({
        sub: 'user-1',
        username: 'admin',
        rol: 'Admin',
        rolId: 'r1',
        exp: Math.floor(Date.now() / 1000) + 7200,
        iat: Math.floor(Date.now() / 1000),
      });

      let result: string | undefined;
      service.refreshToken().subscribe((t) => (result = t));

      const req = httpMock.expectOne('/api/v1/auth/refresh-token');
      expect(req.request.body).toEqual({ refreshToken: 'old-refresh' });

      req.flush({
        success: true,
        data: { accessToken: newToken },
        message: '',
      });

      expect(result).toBe(newToken);
      expect(localStorage.getItem('erp-proarte-access-token')).toBe(newToken);
    });
  });
});
