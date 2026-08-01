/**
 * Interfaces del módulo de autenticación y permisos.
 */

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface UserPayload {
  sub: string;
  username: string;
  rol: string;
  rolId: string;
  exp: number;
  iat: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}

export interface ApiError {
  success: false;
  error: {
    code: string;
    message: string;
  };
}

export interface LoginResponseData {
  accessToken: string;
  refreshToken: string;
  usuario: {
    id: string;
    username: string;
    rol: string;
  };
}

export interface RefreshResponseData {
  accessToken: string;
}

export interface TablaPermisos {
  ver_listado: boolean;
  ver_detalle: boolean;
  crear: boolean;
  editar: boolean;
  eliminar: boolean;
}

export interface PermisosConfig {
  tablas: Record<string, TablaPermisos>;
  contexto: Record<string, string[]>;
}
