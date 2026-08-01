/**
 * Diccionarios centralizados de mensajes de error en español.
 * Nunca se exponen detalles técnicos (stack traces, errores crudos del servidor) al usuario.
 */

/** Mensajes de validación de formularios */
export const errorMessages: Record<string, string> = {
  required: 'Este campo es obligatorio',
  email: 'Ingrese un correo electrónico válido',
  minlength: 'Debe tener al menos {requiredLength} caracteres',
  maxlength: 'No puede exceder {requiredLength} caracteres',
  pattern: 'El formato ingresado no es válido',
  min: 'El valor mínimo permitido es {min}',
  max: 'El valor máximo permitido es {max}',
};

/** Mensajes de error HTTP por código de estado */
export const httpErrorMessages: Record<number, string> = {
  400: 'La solicitud contiene datos inválidos',
  401: 'No tiene autorización. Inicie sesión nuevamente',
  403: 'No tiene permisos para realizar esta acción',
  404: 'El recurso solicitado no fue encontrado',
  409: 'Existe un conflicto con los datos actuales',
  422: 'Los datos enviados no pudieron ser procesados',
  500: 'Ocurrió un error en el servidor. Intente más tarde',
  503: 'El servicio no está disponible temporalmente',
  0: 'No se pudo conectar con el servidor. Verifique su conexión a internet',
};

/** Mensajes generales para operaciones CRUD y estado del sistema */
export const generalMessages: Record<string, string> = {
  loadingError: 'Error al cargar los datos. Intente nuevamente',
  saveSuccess: 'Los cambios se guardaron correctamente',
  saveError: 'No se pudieron guardar los cambios',
  deleteConfirm: '¿Está seguro que desea eliminar este registro?',
  deleteSuccess: 'El registro se eliminó correctamente',
  deleteError: 'No se pudo eliminar el registro',
  sessionExpired: 'Su sesión ha expirado. Inicie sesión nuevamente',
  networkError: 'Error de conexión. Verifique su acceso a internet',
};
