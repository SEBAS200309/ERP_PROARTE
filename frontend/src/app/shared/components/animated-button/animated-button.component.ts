import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  computed,
  inject,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { buttonAnimations } from '../../animations/button.animations';

/** Variantes visuales del botón */
export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost';

@Component({
  selector: 'app-animated-button',
  standalone: true,
  imports: [],
  templateUrl: './animated-button.component.html',
  styleUrl: './animated-button.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: buttonAnimations,
})
export class AnimatedButtonComponent {
  // Inputs
  readonly variant = input<ButtonVariant>('primary');
  readonly disabled = input<boolean>(false);
  readonly loading = input<boolean>(false);
  readonly type = input<'button' | 'submit'>('button');
  readonly ariaLabel = input<string | undefined>(undefined);

  // Output
  readonly buttonClick = output<MouseEvent>();

  // Internal state
  protected readonly pressState = signal<'idle' | 'pressed'>('idle');
  protected readonly hoverState = signal<'out' | 'over'>('out');
  protected readonly ripples = signal<number[]>([]);

  // Reduced motion detection
  private readonly document = inject(DOCUMENT);
  protected readonly reducedMotion: boolean;

  constructor() {
    const window = this.document.defaultView;
    this.reducedMotion =
      !!window &&
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }

  // Computed class binding
  protected readonly buttonClasses = computed(() => {
    const classes = [`btn-${this.variant()}`];
    if (this.loading()) classes.push('btn-loading');
    if (this.disabled()) classes.push('btn-disabled');
    return classes.join(' ');
  });

  // Event handlers
  protected onMouseDown(): void {
    if (this.disabled() || this.loading()) return;
    this.pressState.set('pressed');
  }

  protected onMouseUp(): void {
    this.pressState.set('idle');
  }

  protected onMouseEnter(): void {
    if (this.disabled() || this.loading()) return;
    this.hoverState.set('over');
  }

  protected onMouseLeave(): void {
    this.hoverState.set('out');
    this.pressState.set('idle');
  }

  protected onClick(event: MouseEvent): void {
    if (this.disabled() || this.loading()) {
      event.preventDefault();
      event.stopPropagation();
      return;
    }
    this.addRipple();
    this.buttonClick.emit(event);
  }

  protected onRippleDone(): void {
    this.ripples.update((r) => r.slice(1));
  }

  private addRipple(): void {
    if (this.reducedMotion) return;
    this.ripples.update((r) => [...r, Date.now()]);
  }
}
