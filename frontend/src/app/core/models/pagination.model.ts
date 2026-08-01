/**
 * Modelos de paginación para consumo de API paginada.
 */

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
  [key: string]: any;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
