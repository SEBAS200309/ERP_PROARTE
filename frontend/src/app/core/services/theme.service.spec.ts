import { TestBed } from '@angular/core/testing';
import { ThemeService, Theme } from './theme.service';

// Mock matchMedia for jsdom environment
function mockMatchMedia(prefersDark: boolean): void {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    configurable: true,
    value: vi.fn().mockImplementation((query: string) => ({
      matches: prefersDark,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  });
}

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    mockMatchMedia(false); // Default: prefers light
  });

  afterEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
  });

  function createService(): ThemeService {
    TestBed.configureTestingModule({});
    return TestBed.inject(ThemeService);
  }

  describe('getInitialTheme (via currentTheme signal)', () => {
    it('should default to light when no localStorage and prefers-color-scheme is light', () => {
      mockMatchMedia(false);
      const service = createService();
      expect(service.currentTheme()).toBe('light');
    });

    it('should default to dark when no localStorage and prefers-color-scheme is dark', () => {
      mockMatchMedia(true);
      TestBed.resetTestingModule();
      const service = createService();
      expect(service.currentTheme()).toBe('dark');
    });

    it('should use localStorage value when available (light)', () => {
      localStorage.setItem('erp-proarte-theme', 'light');
      mockMatchMedia(true); // Even if system prefers dark, localStorage wins
      TestBed.resetTestingModule();
      const service = createService();
      expect(service.currentTheme()).toBe('light');
    });

    it('should use localStorage value when available (dark)', () => {
      localStorage.setItem('erp-proarte-theme', 'dark');
      mockMatchMedia(false); // Even if system prefers light, localStorage wins
      TestBed.resetTestingModule();
      const service = createService();
      expect(service.currentTheme()).toBe('dark');
    });
  });

  describe('toggleTheme', () => {
    it('should switch from light to dark', () => {
      const service = createService();
      service.setTheme('light');
      service.toggleTheme();
      expect(service.currentTheme()).toBe('dark');
    });

    it('should switch from dark to light', () => {
      const service = createService();
      service.setTheme('dark');
      service.toggleTheme();
      expect(service.currentTheme()).toBe('light');
    });
  });

  describe('setTheme', () => {
    it('should set theme to dark', () => {
      const service = createService();
      service.setTheme('dark');
      expect(service.currentTheme()).toBe('dark');
    });

    it('should set theme to light', () => {
      const service = createService();
      service.setTheme('dark');
      service.setTheme('light');
      expect(service.currentTheme()).toBe('light');
    });
  });

  describe('localStorage persistence', () => {
    it('should persist theme to localStorage on change', () => {
      const service = createService();
      service.setTheme('dark');
      TestBed.flushEffects();
      expect(localStorage.getItem('erp-proarte-theme')).toBe('dark');
    });

    it('should persist toggled theme to localStorage', () => {
      const service = createService();
      service.setTheme('light');
      TestBed.flushEffects();
      service.toggleTheme();
      TestBed.flushEffects();
      expect(localStorage.getItem('erp-proarte-theme')).toBe('dark');
    });
  });

  describe('data-theme attribute on document.documentElement', () => {
    it('should set data-theme attribute when theme changes', () => {
      const service = createService();
      service.setTheme('dark');
      TestBed.flushEffects();
      expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    });

    it('should update data-theme attribute on toggle', () => {
      const service = createService();
      service.setTheme('light');
      TestBed.flushEffects();
      service.toggleTheme();
      TestBed.flushEffects();
      expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    });
  });
});
