import { Injectable } from '@angular/core';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { Mensaje } from './mensaje.models';

@Injectable({ providedIn: 'root' })
export class MensajeService extends BaseCrudService<Mensaje> {
  protected baseUrl = '/api/v1/mensajes';
}
