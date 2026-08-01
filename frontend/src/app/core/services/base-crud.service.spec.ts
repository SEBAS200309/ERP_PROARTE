import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Injectable } from '@angular/core';

import { BaseCrudService } from './base-crud.service';

interface TestEntity {
  id: string;
  name: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
class TestEntityService extends BaseCrudService<TestEntity> {
  protected baseUrl = '/api/v1/test-entities';
}

describe('BaseCrudService', () => {
  let service: TestEntityService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(TestEntityService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getAll', () => {
    it('should fetch paginated list without params', () => {
      const mockPage = {
        content: [{ id: '1', name: 'Item 1', active: true }],
        totalElements: 1,
        totalPages: 1,
        page: 0,
        size: 10,
      };

      let result: any;
      service.getAll().subscribe((r) => (result = r));

      const req = httpMock.expectOne('/api/v1/test-entities');
      expect(req.request.method).toBe('GET');

      req.flush({ success: true, data: mockPage, message: '' });

      expect(result).toEqual(mockPage);
      expect(result.content).toHaveLength(1);
    });

    it('should send query params when PageParams provided', () => {
      const mockPage = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        page: 1,
        size: 20,
      };

      service.getAll({ page: 1, size: 20, sort: 'name,asc', search: 'test' }).subscribe();

      const req = httpMock.expectOne(
        (r) =>
          r.url === '/api/v1/test-entities' &&
          r.params.get('page') === '1' &&
          r.params.get('size') === '20' &&
          r.params.get('sort') === 'name,asc' &&
          r.params.get('search') === 'test'
      );
      expect(req.request.method).toBe('GET');

      req.flush({ success: true, data: mockPage, message: '' });
    });

    it('should ignore undefined params', () => {
      const mockPage = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        page: 0,
        size: 10,
      };

      service.getAll({ page: 0, size: undefined, search: undefined }).subscribe();

      const req = httpMock.expectOne(
        (r) =>
          r.url === '/api/v1/test-entities' &&
          r.params.get('page') === '0' &&
          !r.params.has('size') &&
          !r.params.has('search')
      );

      req.flush({ success: true, data: mockPage, message: '' });
    });
  });

  describe('getById', () => {
    it('should fetch a single entity by id', () => {
      const mockEntity: TestEntity = { id: 'abc-123', name: 'Test', active: true };

      let result: TestEntity | undefined;
      service.getById('abc-123').subscribe((r) => (result = r));

      const req = httpMock.expectOne('/api/v1/test-entities/abc-123');
      expect(req.request.method).toBe('GET');

      req.flush({ success: true, data: mockEntity, message: '' });

      expect(result).toEqual(mockEntity);
    });
  });

  describe('create', () => {
    it('should POST dto and return created entity', () => {
      const dto = { name: 'New Item', active: true };
      const created: TestEntity = { id: 'new-id', name: 'New Item', active: true };

      let result: TestEntity | undefined;
      service.create(dto).subscribe((r) => (result = r));

      const req = httpMock.expectOne('/api/v1/test-entities');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(dto);

      req.flush({ success: true, data: created, message: 'Creado correctamente' });

      expect(result).toEqual(created);
    });
  });

  describe('update', () => {
    it('should PUT dto and return updated entity', () => {
      const dto = { name: 'Updated' };
      const updated: TestEntity = { id: 'abc-123', name: 'Updated', active: true };

      let result: TestEntity | undefined;
      service.update('abc-123', dto).subscribe((r) => (result = r));

      const req = httpMock.expectOne('/api/v1/test-entities/abc-123');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(dto);

      req.flush({ success: true, data: updated, message: '' });

      expect(result).toEqual(updated);
    });
  });

  describe('delete', () => {
    it('should DELETE by id and return void', () => {
      let completed = false;
      service.delete('abc-123').subscribe(() => (completed = true));

      const req = httpMock.expectOne('/api/v1/test-entities/abc-123');
      expect(req.request.method).toBe('DELETE');

      req.flush({ success: true, data: null, message: 'Eliminado' });

      expect(completed).toBe(true);
    });
  });

  describe('executeFunction', () => {
    it('should POST to execute endpoint with params', () => {
      const params = { status: 'active', limit: 5 };
      const result = { processed: 5 };

      let response: any;
      service.executeFunction('activate', params).subscribe((r) => (response = r));

      const req = httpMock.expectOne('/api/v1/test-entities/execute/activate');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ params });

      req.flush({ success: true, data: result, message: '' });

      expect(response).toEqual(result);
    });
  });

  describe('ApiResponse unwrapping', () => {
    it('should extract data field on success: true', () => {
      const entity: TestEntity = { id: '1', name: 'Test', active: true };

      let result: TestEntity | undefined;
      service.getById('1').subscribe((r) => (result = r));

      httpMock.expectOne('/api/v1/test-entities/1').flush({
        success: true,
        data: entity,
        message: 'OK',
      });

      expect(result).toEqual(entity);
    });

    it('should throw error when success is false with error object', () => {
      let error: any;
      service.getById('bad-id').subscribe({
        error: (e) => (error = e),
      });

      httpMock.expectOne('/api/v1/test-entities/bad-id').flush({
        success: false,
        error: { code: 'ERR_NOT_FOUND', message: 'Recurso no encontrado' },
        message: '',
      });

      expect(error).toBeInstanceOf(Error);
      expect(error.message).toBe('Recurso no encontrado');
    });

    it('should throw error with message field when no error object', () => {
      let error: any;
      service.create({ name: '' }).subscribe({
        error: (e) => (error = e),
      });

      httpMock.expectOne('/api/v1/test-entities').flush({
        success: false,
        data: null,
        message: 'Datos inválidos',
      });

      expect(error).toBeInstanceOf(Error);
      expect(error.message).toBe('Datos inválidos');
    });

    it('should throw generic error when success is false with no message', () => {
      let error: any;
      service.getById('x').subscribe({
        error: (e) => (error = e),
      });

      httpMock.expectOne('/api/v1/test-entities/x').flush({
        success: false,
        data: null,
        message: '',
      });

      expect(error).toBeInstanceOf(Error);
      expect(error.message).toBe('Error desconocido del servidor');
    });
  });
});
