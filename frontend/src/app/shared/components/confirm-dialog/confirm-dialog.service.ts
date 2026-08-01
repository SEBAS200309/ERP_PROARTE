import {
  Injectable,
  ApplicationRef,
  createComponent,
  EnvironmentInjector,
  inject,
} from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { take } from 'rxjs/operators';

import { ConfirmDialogComponent } from './confirm-dialog.component';

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private readonly appRef = inject(ApplicationRef);
  private readonly injector = inject(EnvironmentInjector);

  /**
   * Abre el diálogo de confirmación y retorna un Observable<boolean>.
   * Emite true si el usuario confirma, false si cancela.
   *
   * @param message Mensaje del diálogo (default: '¿Está seguro que desea eliminar este registro?')
   * @param title Título del diálogo (default: 'Confirmar eliminación')
   */
  confirm(
    message = '¿Está seguro que desea eliminar este registro?',
    title = 'Confirmar eliminación'
  ): Observable<boolean> {
    const result$ = new Subject<boolean>();

    const componentRef = createComponent(ConfirmDialogComponent, {
      environmentInjector: this.injector,
    });

    // Set inputs
    componentRef.setInput('visible', true);
    componentRef.setInput('title', title);
    componentRef.setInput('message', message);

    // Subscribe to outputs
    componentRef.instance.confirmed.subscribe(() => {
      result$.next(true);
      result$.complete();
      this.destroyDialog(componentRef);
    });

    componentRef.instance.cancelled.subscribe(() => {
      result$.next(false);
      result$.complete();
      this.destroyDialog(componentRef);
    });

    // Attach to DOM
    this.appRef.attachView(componentRef.hostView);
    document.body.appendChild(componentRef.location.nativeElement);

    return result$.asObservable().pipe(take(1));
  }

  private destroyDialog(componentRef: any): void {
    this.appRef.detachView(componentRef.hostView);
    componentRef.destroy();
  }
}
