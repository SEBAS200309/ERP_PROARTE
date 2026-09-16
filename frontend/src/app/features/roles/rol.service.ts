import { Injectable } from '@angular/core';
import { BaseCrudService } from '../../core/services/base-crud.service';
import { Rol } from './rol.models';

@Injectable({ providedIn: 'root' })
export class RolService extends BaseCrudService<Rol> {
    protected baseUrl = '/api/v1/roles';
}