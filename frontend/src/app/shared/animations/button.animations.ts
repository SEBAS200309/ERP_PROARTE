import {
  trigger,
  state,
  style,
  transition,
  animate,
} from '@angular/animations';

/**
 * Animaciones reutilizables para botones.
 * Se aplican en el AnimatedButtonComponent y pueden usarse en otros componentes.
 *
 * - buttonPress: escala al presionar
 * - buttonHover: elevación al pasar el cursor
 * - buttonRipple: efecto de expansión al hacer click
 */
export const buttonAnimations = [
  trigger('buttonPress', [
    state('idle', style({ transform: 'scale(1)' })),
    state('pressed', style({ transform: 'scale(0.95)' })),
    transition('idle <=> pressed', animate('100ms ease-in-out')),
  ]),
  trigger('buttonHover', [
    state(
      'out',
      style({
        transform: 'translateY(0)',
        boxShadow: '0 2px 4px rgba(107, 63, 160, 0.2)',
      })
    ),
    state(
      'over',
      style({
        transform: 'translateY(-2px)',
        boxShadow: '0 6px 12px rgba(107, 63, 160, 0.3)',
      })
    ),
    transition('out <=> over', animate('200ms cubic-bezier(0.4, 0, 0.2, 1)')),
  ]),
  trigger('buttonRipple', [
    transition(':enter', [
      style({ opacity: 0.6, transform: 'scale(0)' }),
      animate('400ms ease-out', style({ opacity: 0, transform: 'scale(2.5)' })),
    ]),
  ]),
];
