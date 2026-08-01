import { TestBed } from '@angular/core/testing';

import { ErrorMessageService } from './error-message.service';

describe('ErrorMessageService', () => {
  let service: ErrorMessageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ErrorMessageService);
  });

  describe('getValidationMessage', () => {
    it('should return required message', () => {
      expect(service.getValidationMessage('required')).toBe('Este campo es obligatorio');
    });

    it('should return email message', () => {
      expect(service.getValidationMessage('email')).toBe(
        'Ingrese un correo electrónico válido'
      );
    });

    it('should return pattern message', () => {
      expect(service.getValidationMessage('pattern')).toBe(
        'El formato ingresado no es válido'
      );
    });

    it('should interpolate minlength params', () => {
      const result = service.getValidationMessage('minlength', { requiredLength: 8 });
      expect(result).toBe('Debe tener al menos 8 caracteres');
    });

    it('should interpolate maxlength params', () => {
      const result = service.getValidationMessage('maxlength', { requiredLength: 50 });
      expect(result).toBe('No puede exceder 50 caracteres');
    });

    it('should interpolate min params', () => {
      const result = service.getValidationMessage('min', { min: 1 });
      expect(result).toBe('El valor mínimo permitido es 1');
    });

    it('should interpolate max params', () => {
      const result = service.getValidationMessage('max', { max: 100 });
      expect(result).toBe('El valor máximo permitido es 100');
    });

    it('should return template without replacement when params are missing', () => {
      const result = service.getValidationMessage('minlength');
      expect(result).toBe('Debe tener al menos {requiredLength} caracteres');
    });

    it('should keep placeholder when specific param key is not provided', () => {
      const result = service.getValidationMessage('minlength', { otherKey: 5 });
      expect(result).toBe('Debe tener al menos {requiredLength} caracteres');
    });

    it('should return fallback message for unknown error key', () => {
      expect(service.getValidationMessage('unknownValidator')).toBe(
        'El valor ingresado no es válido'
      );
    });
  });

  describe('getHttpErrorMessage', () => {
    it('should return message for 400', () => {
      expect(service.getHttpErrorMessage(400)).toBe(
        'La solicitud contiene datos inválidos'
      );
    });

    it('should return message for 401', () => {
      expect(service.getHttpErrorMessage(401)).toBe(
        'No tiene autorización. Inicie sesión nuevamente'
      );
    });

    it('should return message for 403', () => {
      expect(service.getHttpErrorMessage(403)).toBe(
        'No tiene permisos para realizar esta acción'
      );
    });

    it('should return message for 404', () => {
      expect(service.getHttpErrorMessage(404)).toBe(
        'El recurso solicitado no fue encontrado'
      );
    });

    it('should return message for 409', () => {
      expect(service.getHttpErrorMessage(409)).toBe(
        'Existe un conflicto con los datos actuales'
      );
    });

    it('should return message for 422', () => {
      expect(service.getHttpErrorMessage(422)).toBe(
        'Los datos enviados no pudieron ser procesados'
      );
    });

    it('should return message for 500', () => {
      expect(service.getHttpErrorMessage(500)).toBe(
        'Ocurrió un error en el servidor. Intente más tarde'
      );
    });

    it('should return message for 503', () => {
      expect(service.getHttpErrorMessage(503)).toBe(
        'El servicio no está disponible temporalmente'
      );
    });

    it('should return network error message for status 0', () => {
      expect(service.getHttpErrorMessage(0)).toBe(
        'No se pudo conectar con el servidor. Verifique su conexión a internet'
      );
    });

    it('should return fallback message for unknown status codes', () => {
      expect(service.getHttpErrorMessage(418)).toBe(
        'Ocurrió un error inesperado. Intente más tarde'
      );
    });

    it('should return fallback message for status 502', () => {
      expect(service.getHttpErrorMessage(502)).toBe(
        'Ocurrió un error inesperado. Intente más tarde'
      );
    });
  });

  describe('getGeneralMessage', () => {
    it('should return loading error message', () => {
      expect(service.getGeneralMessage('loadingError')).toBe(
        'Error al cargar los datos. Intente nuevamente'
      );
    });

    it('should return save success message', () => {
      expect(service.getGeneralMessage('saveSuccess')).toBe(
        'Los cambios se guardaron correctamente'
      );
    });

    it('should return save error message', () => {
      expect(service.getGeneralMessage('saveError')).toBe(
        'No se pudieron guardar los cambios'
      );
    });

    it('should return delete confirm message', () => {
      expect(service.getGeneralMessage('deleteConfirm')).toBe(
        '¿Está seguro que desea eliminar este registro?'
      );
    });

    it('should return delete success message', () => {
      expect(service.getGeneralMessage('deleteSuccess')).toBe(
        'El registro se eliminó correctamente'
      );
    });

    it('should return delete error message', () => {
      expect(service.getGeneralMessage('deleteError')).toBe(
        'No se pudo eliminar el registro'
      );
    });

    it('should return session expired message', () => {
      expect(service.getGeneralMessage('sessionExpired')).toBe(
        'Su sesión ha expirado. Inicie sesión nuevamente'
      );
    });

    it('should return network error message', () => {
      expect(service.getGeneralMessage('networkError')).toBe(
        'Error de conexión. Verifique su acceso a internet'
      );
    });

    it('should return fallback message for unknown key', () => {
      expect(service.getGeneralMessage('unknownKey')).toBe(
        'Ocurrió un error inesperado'
      );
    });
  });
});
