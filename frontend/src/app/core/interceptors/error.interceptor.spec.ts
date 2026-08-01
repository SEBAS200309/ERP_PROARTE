import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';

import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should map 400 to Spanish error message', () => {
    let error: { status: number; message: string } | undefined;

    httpClient.get('/api/v1/test').subscribe({
      error: (e) => (error = e),
    });

    httpMock.expectOne('/api/v1/test').flush(null, { status: 400, statusText: 'Bad Request' });

    expect(error?.status).toBe(400);
    expect(error?.message).toBe('La solicitud contiene datos inválidos');
  });

  it('should map 401 to Spanish error message and redirect to login', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    let error: { status: number; message: string } | undefined;

    httpClient.get('/api/v1/test').subscribe({
      error: (e) => (error = e),
    });

    httpMock.expectOne('/api/v1/test').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(error?.status).toBe(401);
    expect(error?.message).toBe('No tiene autorización. Inicie sesión nuevamente');
    expect(navigateSpy).toHaveBeenCalledWith(['/auth/login']);
  });

  it('should map 403 to Spanish error message', () => {
    let error: { status: number; message: string } | undefined;

    httpClient.get('/api/v1/test').subscribe({
      error: (e) => (error = e),
    });

    httpMock.expectOne('/api/v1/test').flush(null, { status: 403, statusText: 'Forbidden' });

    expect(error?.status).toBe(403);
    expect(error?.message).toBe('No tiene permisos para realizar esta acción');
  });

  it('should map 404 to Spanish error message', () => {
    let error: { status: number; message: string } | undefined;

    httpClient.get('/api/v1/test').subscribe({
      error: (e) => (error = e),
    });

    httpMock.expectOne('/api/v1/test').flush(null, { status: 404, statusText: 'Not Found' });

    expect(error?.status).toBe(404);
    expect(error?.message).toBe('El recurso solicitado no fue encontrado');
  });

  it('should map 500 to Spanish error message', () => {
    let error: { status: number; message: string } | undefined;

    httpClient.get('/api/v1/test').subscribe({
      error: (e) => (error = e),
    });

    httpMock.expectOne('/api/v1/test').flush(null, { status: 500, statusText: 'Server Error' });

    expect(error?.status).toBe(500);
    expect(error?.message).toBe('Ocurrió un error en el servidor. Intente más tarde');
  });

  it('should use server error message when provided', () => {
    let error: { status: number; message: string } | undefined;

    httpClient.get('/api/v1/test').subscribe({
      error: (e) => (error = e),
    });

    httpMock.expectOne('/api/v1/test').flush(
      { success: false, error: { code: 'ERR_BUSINESS', message: 'Cotización ya fue aprobada' } },
      { status: 422, statusText: 'Unprocessable Entity' }
    );

    expect(error?.message).toBe('Cotización ya fue aprobada');
  });

  it('should map network error (status 0) to connection message', () => {
    let error: { status: number; message: string } | undefined;

    httpClient.get('/api/v1/test').subscribe({
      error: (e) => (error = e),
    });

    httpMock.expectOne('/api/v1/test').error(new ProgressEvent('error'), { status: 0 });

    expect(error?.status).toBe(0);
    expect(error?.message).toBe('No se pudo conectar con el servidor. Verifique su conexión a internet');
  });
});
