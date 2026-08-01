import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { jwtInterceptor } from './jwt.interceptor';

describe('jwtInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should attach Authorization header for API requests when token exists', () => {
    localStorage.setItem('erp-proarte-access-token', 'my-jwt-token');

    httpClient.get('/api/v1/usuarios').subscribe();

    const req = httpMock.expectOne('/api/v1/usuarios');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt-token');
  });

  it('should NOT attach header when no token exists', () => {
    httpClient.get('/api/v1/usuarios').subscribe();

    const req = httpMock.expectOne('/api/v1/usuarios');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });

  it('should NOT attach header for login endpoint', () => {
    localStorage.setItem('erp-proarte-access-token', 'my-jwt-token');

    httpClient.post('/api/v1/auth/login', {}).subscribe();

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });

  it('should NOT attach header for refresh-token endpoint', () => {
    localStorage.setItem('erp-proarte-access-token', 'my-jwt-token');

    httpClient.post('/api/v1/auth/refresh-token', {}).subscribe();

    const req = httpMock.expectOne('/api/v1/auth/refresh-token');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });

  it('should NOT attach header for non-API requests', () => {
    localStorage.setItem('erp-proarte-access-token', 'my-jwt-token');

    httpClient.get('/assets/config.json').subscribe();

    const req = httpMock.expectOne('/assets/config.json');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });
});
