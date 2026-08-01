import { Injectable } from '@angular/core';

import { errorMessages, httpErrorMessages, generalMessages } from './error-messages';

const DEFAULT_HTTP_MESSAGE = 'Ocurrió un error inesperado. Intente más tarde';
const DEFAULT_VALIDATION_MESSAGE = 'El valor ingresado no es válido';
const DEFAULT_GENERAL_MESSAGE = 'Ocurrió un error inesperado';

/**
 * Servicio centralizado de resolución de mensajes de error.
 * Nunca expone stack traces ni detalles técnicos al usuario final.
 */
@Injectable({ providedIn: 'root' })
export class ErrorMessageService {
  /**
   * Resuelve un error de validación de formulario a un mensaje en español.
   * Soporta interpolación de parámetros (ej: {requiredLength}, {min}, {max}).
   *
   * @param errorKey - Clave del error de validación (required, email, minlength, etc.)
   * @param params - Parámetros opcionales para interpolación
   * @returns Mensaje de error en español
   */
  getValidationMessage(errorKey: string, params?: Record<string, any>): string {
    const template = errorMessages[errorKey];

    if (!template) {
      return DEFAULT_VALIDATION_MESSAGE;
    }

    if (!params) {
      return template;
    }

    return template.replace(/\{(\w+)\}/g, (_, key) => {
      return params[key] !== undefined ? String(params[key]) : `{${key}}`;
    });
  }

  /**
   * Resuelve un código de estado HTTP a un mensaje en español.
   *
   * @param statusCode - Código de estado HTTP
   * @returns Mensaje de error en español
   */
  getHttpErrorMessage(statusCode: number): string {
    return httpErrorMessages[statusCode] ?? DEFAULT_HTTP_MESSAGE;
  }

  /**
   * Resuelve una clave de mensaje general a su texto en español.
   *
   * @param key - Clave del mensaje (loadingError, saveSuccess, etc.)
   * @returns Mensaje en español
   */
  getGeneralMessage(key: string): string {
    return generalMessages[key] ?? DEFAULT_GENERAL_MESSAGE;
  }
}
