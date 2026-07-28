import { Injectable, signal, effect } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly storageKey = 'erp-proarte-theme';
  readonly currentTheme = signal<Theme>(this.getInitialTheme());

  constructor() {
    effect(() => {
      const theme = this.currentTheme();
      document.documentElement.setAttribute('data-theme', theme);
      localStorage.setItem(this.storageKey, theme);
    });
  }

  toggleTheme(): void {
    this.currentTheme.update(current => current === 'light' ? 'dark' : 'light');
  }

  setTheme(theme: Theme): void {
    this.currentTheme.set(theme);
  }

  private getInitialTheme(): Theme {
    const stored = localStorage.getItem(this.storageKey) as Theme | null;
    if (stored) {
      return stored;
    }
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    return prefersDark ? 'dark' : 'light';
  }
}
