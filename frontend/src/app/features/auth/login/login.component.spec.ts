import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: { login: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authService = { login: vi.fn() };
    router = { navigate: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule, AnimatedButtonComponent, NoopAnimationsModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render the login form', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('form')).toBeTruthy();
    expect(compiled.querySelector('#username')).toBeTruthy();
    expect(compiled.querySelector('#password')).toBeTruthy();
    expect(compiled.querySelector('.brand-name')?.textContent).toContain('PRO ARTE');
  });

  it('should show validation errors for required fields', () => {
    const form = (component as any).loginForm;
    form.get('username')!.markAsTouched();
    form.get('password')!.markAsTouched();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const errors = compiled.querySelectorAll('.field-error');
    expect(errors.length).toBe(2);
    expect(errors[0].textContent).toContain('Este campo es obligatorio');
    expect(errors[1].textContent).toContain('Este campo es obligatorio');
  });

  it('should call AuthService.login on submit', () => {
    authService.login.mockReturnValue(of(true));

    const form = (component as any).loginForm;
    form.get('username')!.setValue('admin');
    form.get('password')!.setValue('password123');
    fixture.detectChanges();

    (component as any).onSubmit();

    expect(authService.login).toHaveBeenCalledWith('admin', 'password123');
  });

  it('should show error message on failed login', () => {
    authService.login.mockReturnValue(of(false));

    const form = (component as any).loginForm;
    form.get('username')!.setValue('admin');
    form.get('password')!.setValue('wrong');
    fixture.detectChanges();

    (component as any).onSubmit();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const errorEl = compiled.querySelector('.login-error');
    expect(errorEl).toBeTruthy();
    expect(errorEl?.textContent).toContain('Credenciales incorrectas');
  });

  it('should redirect to /dashboard on successful login', () => {
    authService.login.mockReturnValue(of(true));

    const form = (component as any).loginForm;
    form.get('username')!.setValue('admin');
    form.get('password')!.setValue('password123');
    fixture.detectChanges();

    (component as any).onSubmit();

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should show loading state during request', () => {
    expect((component as any).loading()).toBe(false);

    (component as any).loading.set(true);
    fixture.detectChanges();

    expect((component as any).loading()).toBe(true);
  });

  it('should show connection error on network failure', () => {
    authService.login.mockReturnValue(throwError(() => new Error('Network error')));

    const form = (component as any).loginForm;
    form.get('username')!.setValue('admin');
    form.get('password')!.setValue('password123');
    fixture.detectChanges();

    (component as any).onSubmit();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const errorEl = compiled.querySelector('.login-error');
    expect(errorEl?.textContent).toContain('Error de conexión');
  });

  it('should not submit when form is invalid', () => {
    (component as any).onSubmit();
    expect(authService.login).not.toHaveBeenCalled();
  });
});
