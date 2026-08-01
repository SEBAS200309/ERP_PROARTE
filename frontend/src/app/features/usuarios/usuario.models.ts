/**
 * Modelos del módulo de Usuarios.
 */

export interface Usuario {
  id: string;
  username: string;
  nombreCompleto: string;
  email: string;
  rolId: string;
  rolNombre: string;
  activo: boolean;
  createdAt: string;
}

export interface CreateUsuarioRequest {
  username: string;
  password: string;
  nombreCompleto: string;
  email: string;
  rolId: string;
}

export interface UpdateUsuarioRequest {
  username: string;
  nombreCompleto: string;
  email: string;
  rolId: string;
  password?: string;
}

export interface Rol {
  id: string;
  nombre: string;
  descripcion: string;
  activo: boolean;
}
