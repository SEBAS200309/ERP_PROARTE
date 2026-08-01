import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, signal } from '@angular/core';
import { SearchFilterComponent } from './search-filter.component';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

// Test host component to pass inputs via signals
@Component({
  standalone: true,
  imports: [SearchFilterComponent],
  template: `
    <app-search-filter
      [placeholder]="placeholder()"
      [debounceTime]="debounceTime()"
      [value]="value()"
      (searchChange)="onSearchChange($event)"
    />
  `,
})
class TestHostComponent {
  readonly placeholder = signal('Buscar...');
  readonly debounceTime = signal(300);
  readonly value = signal('');
  readonly emittedValues: string[] = [];

  onSearchChange(term: string): void {
    this.emittedValues.push(term);
  }
}

describe('SearchFilterComponent', () => {
  let hostFixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    vi.useFakeTimers();

    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();

    hostFixture = TestBed.createComponent(TestHostComponent);
    host = hostFixture.componentInstance;
    hostFixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  function getInput(): HTMLInputElement {
    return hostFixture.nativeElement.querySelector('.search-filter__input');
  }

  function getClearButton(): HTMLButtonElement | null {
    return hostFixture.nativeElement.querySelector('.search-filter__clear');
  }

  function typeInInput(value: string): void {
    const input = getInput();
    input.value = value;
    input.dispatchEvent(new Event('input'));
    hostFixture.detectChanges();
  }

  it('should create the component', () => {
    const component = hostFixture.nativeElement.querySelector('app-search-filter');
    expect(component).toBeTruthy();
  });

  it('should display the default placeholder', () => {
    const input = getInput();
    expect(input.placeholder).toBe('Buscar...');
  });

  it('should display a custom placeholder', () => {
    host.placeholder.set('Buscar personas...');
    hostFixture.detectChanges();
    const input = getInput();
    expect(input.placeholder).toBe('Buscar personas...');
  });

  it('should not show clear button when input is empty', () => {
    expect(getClearButton()).toBeNull();
  });

  it('should show clear button when text is entered', () => {
    typeInInput('test');
    expect(getClearButton()).toBeTruthy();
  });

  it('should emit search term after debounce', () => {
    typeInInput('angular');
    // Before debounce, nothing emitted
    expect(host.emittedValues.length).toBe(0);

    vi.advanceTimersByTime(300);
    expect(host.emittedValues).toEqual(['angular']);
  });

  it('should debounce rapid input changes', () => {
    typeInInput('a');
    vi.advanceTimersByTime(100);
    typeInInput('an');
    vi.advanceTimersByTime(100);
    typeInInput('ang');
    vi.advanceTimersByTime(300);

    // Only the last value after debounce
    expect(host.emittedValues).toEqual(['ang']);
  });

  it('should clear input and emit empty string on clear button click', () => {
    typeInInput('some text');
    vi.advanceTimersByTime(300);
    host.emittedValues.length = 0; // reset

    const clearBtn = getClearButton()!;
    clearBtn.click();
    hostFixture.detectChanges();

    expect(getInput().value).toBe('');
    // Clear emits immediately (no debounce needed)
    expect(host.emittedValues).toEqual(['']);
  });

  it('should clear input on Escape key', () => {
    typeInInput('hello');
    vi.advanceTimersByTime(300);
    host.emittedValues.length = 0;

    const input = getInput();
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    hostFixture.detectChanges();

    expect(getInput().value).toBe('');
    expect(host.emittedValues).toEqual(['']);
  });

  it('should have aria-label on input for accessibility', () => {
    const input = getInput();
    expect(input.getAttribute('aria-label')).toBe('Buscar');
  });

  it('should have aria-label on clear button', () => {
    typeInInput('test');
    const clearBtn = getClearButton()!;
    expect(clearBtn.getAttribute('aria-label')).toBe('Limpiar búsqueda');
  });

  it('should not emit duplicate consecutive values', () => {
    typeInInput('hello');
    vi.advanceTimersByTime(300);
    typeInInput('hello');
    vi.advanceTimersByTime(300);

    // distinctUntilChanged prevents duplicate emission
    expect(host.emittedValues).toEqual(['hello']);
  });

  it('should sync initial value from input', () => {
    host.value.set('initial');
    hostFixture.detectChanges();

    const input = getInput();
    expect(input.value).toBe('initial');
  });
});
