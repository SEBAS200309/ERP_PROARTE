import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  OnInit,
  OnDestroy,
  effect,
} from '@angular/core';
import { Subject, debounceTime, distinctUntilChanged, Subscription } from 'rxjs';

@Component({
  selector: 'app-search-filter',
  standalone: true,
  templateUrl: './search-filter.component.html',
  styleUrl: './search-filter.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchFilterComponent implements OnInit, OnDestroy {
  // Inputs
  readonly placeholder = input<string>('Buscar...');
  readonly debounceTime = input<number>(300);
  readonly value = input<string>('');

  // Outputs
  readonly searchChange = output<string>();

  // Internal state
  protected readonly searchTerm = signal<string>('');

  private readonly searchSubject = new Subject<string>();
  private subscription: Subscription | null = null;

  constructor() {
    // Sync external value input to internal state
    effect(() => {
      this.searchTerm.set(this.value());
    });
  }

  ngOnInit(): void {
    this.subscription = this.searchSubject
      .pipe(debounceTime(this.debounceTime()), distinctUntilChanged())
      .subscribe((term) => {
        this.searchChange.emit(term);
      });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.searchSubject.complete();
  }

  protected onInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchTerm.set(value);
    this.searchSubject.next(value);
  }

  protected onClear(): void {
    this.searchTerm.set('');
    this.searchSubject.next('');
    this.searchChange.emit('');
  }

  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.onClear();
    }
  }
}
