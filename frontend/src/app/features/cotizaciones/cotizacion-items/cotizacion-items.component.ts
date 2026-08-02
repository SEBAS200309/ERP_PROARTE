import { Component, OnInit, inject, signal, input, output } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { CotizacionService } from '../cotizacion.service';
import {
  CotizacionItemRequest,
  ServicioOption,
  DescuentoRecargoOption,
} from '../cotizacion.models';

@Component({
  selector: 'app-cotizacion-items',
  standalone: true,
  imports: [DecimalPipe, FormsModule, AnimatedButtonComponent],
  templateUrl: './cotizacion-items.component.html',
  styleUrl: './cotizacion-items.component.scss',
})
export class CotizacionItemsComponent implements OnInit {
  private readonly cotizacionService = inject(CotizacionService);

  readonly items = input<CotizacionItemRequest[]>([]);
  readonly itemsChange = output<CotizacionItemRequest[]>();

  protected readonly servicios = signal<ServicioOption[]>([]);
  protected readonly descuentosRecargos = signal<DescuentoRecargoOption[]>([]);
  protected readonly editableItems = signal<EditableItem[]>([]);

  ngOnInit(): void {
    this.loadCatalogos();
    this.syncItemsFromInput();
  }

  protected addItem(): void {
    const current = this.editableItems();
    const newItem: EditableItem = {
      servicioId: '',
      cantidad: 1,
      precioUnitario: 0,
      descuentoRecargoId: '',
    };
    this.editableItems.set([...current, newItem]);
    this.emitItems();
  }

  protected removeItem(index: number): void {
    const current = this.editableItems();
    const updated = current.filter((_, i) => i !== index);
    this.editableItems.set(updated);
    this.emitItems();
  }

  protected updateItem(index: number, field: keyof EditableItem, value: any): void {
    const current = [...this.editableItems()];
    current[index] = { ...current[index], [field]: value };
    this.editableItems.set(current);
    this.emitItems();
  }

  protected getSubtotal(item: EditableItem): number {
    return item.cantidad * item.precioUnitario;
  }

  protected getServicioNombre(servicioId: string): string {
    const servicio = this.servicios().find((s) => s.id === servicioId);
    return servicio?.nombre || '';
  }

  private syncItemsFromInput(): void {
    const inputItems = this.items();
    if (inputItems && inputItems.length > 0) {
      const editable: EditableItem[] = inputItems.map((item) => ({
        servicioId: item.servicioId,
        cantidad: item.cantidad,
        precioUnitario: item.precioUnitario,
        descuentoRecargoId: item.descuentoRecargoId || '',
      }));
      this.editableItems.set(editable);
    }
  }

  private loadCatalogos(): void {
    this.cotizacionService.getServicios().subscribe({
      next: (servicios) => this.servicios.set(servicios),
    });
    this.cotizacionService.getDescuentosRecargos().subscribe({
      next: (descuentos) => this.descuentosRecargos.set(descuentos),
    });
  }

  private emitItems(): void {
    const items: CotizacionItemRequest[] = this.editableItems()
      .filter((item) => item.servicioId)
      .map((item) => ({
        servicioId: item.servicioId,
        cantidad: item.cantidad,
        precioUnitario: item.precioUnitario,
        descuentoRecargoId: item.descuentoRecargoId || undefined,
      }));
    this.itemsChange.emit(items);
  }
}

interface EditableItem {
  servicioId: string;
  cantidad: number;
  precioUnitario: number;
  descuentoRecargoId: string;
}
