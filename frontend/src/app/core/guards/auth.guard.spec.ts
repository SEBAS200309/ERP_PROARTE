import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authService: AuthService;
  let router: Router;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/dashboard' } as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideRouter([]),
      ],
    });

    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should allow access when authenticated', () => {
    vi.spyOn(authService, 'isAuthenticated').mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() => authGuard(mockRoute, mockState));

    expect(result).toBe(true);
  });

  it('should redirect to login when not authenticated', () => {
    vi.spyOn(authService, 'isAuthenticated').mockReturnValue(false);
    const createUrlTreeSpy = vi.spyOn(router, 'createUrlTree');

    TestBed.runInInjectionContext(() => authGuard(mockRoute, mockState));

    expect(createUrlTreeSpy).toHaveBeenCalledWith(['/auth/login']);
  });
});
