import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, map, tap, catchError, of } from 'rxjs';

import {
  ApiResponse,
  AuthTokens,
  LoginCredentials,
  LoginResponseData,
  RefreshResponseData,
  UserPayload,
} from '../models/auth.models';

const STORAGE_KEYS = {
  ACCESS_TOKEN: 'erp-proarte-access-token',
  REFRESH_TOKEN: 'erp-proarte-refresh-token',
} as const;

const AUTH_API = '/api/v1/auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly userPayload = signal<UserPayload | null>(this.decodeStoredToken());

  readonly currentUser = computed(() => {
    const payload = this.userPayload();
    if (!payload) return null;
    return {
      id: payload.sub,
      username: payload.username,
      rol: payload.rol,
      rolId: payload.rolId,
    };
  });

  login(username: string, password: string): Observable<boolean> {
    const credentials: LoginCredentials = { username, password };

    return this.http
      .post<ApiResponse<LoginResponseData>>(`${AUTH_API}/login`, credentials)
      .pipe(
        tap((response) => {
          if (response.success) {
            this.storeTokens({
              accessToken: response.data.accessToken,
              refreshToken: response.data.refreshToken,
            });
            this.userPayload.set(this.decodeToken(response.data.accessToken));
          }
        }),
        map((response) => response.success),
        catchError(() => of(false))
      );
  }

  logout(): void {
    this.http
      .post(`${AUTH_API}/logout`, { refreshToken: this.getRefreshToken() })
      .subscribe({ error: () => {} });

    this.clearTokens();
    this.userPayload.set(null);
    this.router.navigate(['/auth/login']);
  }

  refreshToken(): Observable<string> {
    const refreshToken = this.getRefreshToken();

    return this.http
      .post<ApiResponse<RefreshResponseData>>(`${AUTH_API}/refresh-token`, { refreshToken })
      .pipe(
        tap((response) => {
          if (response.success) {
            localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.data.accessToken);
            this.userPayload.set(this.decodeToken(response.data.accessToken));
          }
        }),
        map((response) => response.data.accessToken)
      );
  }

  getAccessToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
  }

  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;

    const payload = this.decodeToken(token);
    if (!payload) return false;

    const now = Math.floor(Date.now() / 1000);
    return payload.exp > now;
  }

  clearSession(): void {
    this.clearTokens();
    this.userPayload.set(null);
  }

  private getRefreshToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
  }

  private storeTokens(tokens: AuthTokens): void {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, tokens.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, tokens.refreshToken);
  }

  private clearTokens(): void {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
  }

  private decodeStoredToken(): UserPayload | null {
    const token = this.getAccessToken();
    if (!token) return null;
    return this.decodeToken(token);
  }

  private decodeToken(token: string): UserPayload | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;

      const payload = JSON.parse(atob(parts[1]));
      return payload as UserPayload;
    } catch {
      return null;
    }
  }
}
