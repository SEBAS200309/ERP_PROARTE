import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-placeholder',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="placeholder-container">
      <div class="placeholder-icon">🚧</div>
      <h2>Módulo en construcción</h2>
      <p>Esta sección estará disponible próximamente.</p>
      <a routerLink="/dashboard" class="placeholder-link">Volver al dashboard</a>
    </div>
  `,
  styles: [`
    .placeholder-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 60vh;
      text-align: center;
      padding: 2rem;
    }

    .placeholder-icon {
      font-size: 4rem;
      margin-bottom: 1rem;
    }

    h2 {
      color: var(--color-text-primary);
      margin-bottom: 0.5rem;
    }

    p {
      color: var(--color-text-secondary);
      margin-bottom: 1.5rem;
    }

    .placeholder-link {
      color: var(--color-primary);
      text-decoration: none;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  `]
})
export class PlaceholderComponent {}
