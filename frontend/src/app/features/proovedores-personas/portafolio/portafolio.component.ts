import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DecimalPipe } from '@angular/common';

import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ProveedorService } from '../../proveedores/proveedor.service';
import { PortafolioItem, ServicioOption } from '../../proveedores/proveedor.models';

@Component({
    selector: 'app-portafolio',
    standalone: true,
    imports: [ReactiveFormsModule, DecimalPipe, AnimatedButtonComponent, ConfirmDialogComponent],
    templateUrl: './portafolio.component.html',
    styleUrl: './portafolio.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PortafolioComponent implements OnInit {
    private readonly proveedorService = inject(ProveedorService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly fb = inject(FormBuilder);

    protected readonly loading = signal(false);
    protected readonly saving = signal(false);
    protected readonly portafolio = signal<PortafolioItem[]>([]);
    protected readonly servicios = signal<ServicioOption[]>([]);
    protected readonly showForm = signal(false);
    protected readonly editingItem = signal<PortafolioItem | null>(null);
    protected readonly showDeleteDialog = signal(false);

    protected form!: FormGroup;
    private proveedorId = '';
    private itemToDelete: PortafolioItem | null = null;

    private serviciosMap = new Map<string, string>();

    ngOnInit(): void {
        this.proveedorId = this.route.snapshot.paramMap.get('id') ?? '';
        this.buildForm();
        this.loadServicios();
        this.loadPortafolio();
    }

    protected getServicioNombre(servicioId: string): string {
        return this.serviciosMap.get(servicioId) ?? '—';
    }

    protected openAddForm(): void {
        this.editingItem.set(null);
        this.form.reset({ servicioId: '', precioUnitario: '' });
        this.showForm.set(true);
    }

    protected openEditForm(item: PortafolioItem): void {
        this.editingItem.set(item);
        this.form.patchValue({
            servicioId: item.servicioId,
            precioUnitario: item.precioUnitario,
        });
        this.showForm.set(true);
    }

    protected cancelForm(): void {
        this.showForm.set(false);
        this.editingItem.set(null);
        this.form.reset();
    }

    protected saveItem(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.saving.set(true);
        const formValue = this.form.getRawValue();
        const editing = this.editingItem();

        if (editing) {
            this.proveedorService
                .updatePortafolio(editing.id, {
                    servicioId: formValue.servicioId,
                    precioUnitario: Number(formValue.precioUnitario),
                })
                .subscribe({
                    next: () => {
                        this.saving.set(false);
                        this.showForm.set(false);
                        this.editingItem.set(null);
                        this.loadPortafolio();
                    },
                    error: () => {
                        this.saving.set(false);
                    },
                });
        } else {
            this.proveedorService
                .createPortafolio(this.proveedorId, {
                    servicioId: formValue.servicioId,
                    precioUnitario: Number(formValue.precioUnitario),
                })
                .subscribe({
                    next: () => {
                        this.saving.set(false);
                        this.showForm.set(false);
                        this.loadPortafolio();
                    },
                    error: () => {
                        this.saving.set(false);
                    },
                });
        }
    }

    protected onDeleteItem(item: PortafolioItem): void {
        this.itemToDelete = item;
        this.showDeleteDialog.set(true);
    }

    protected confirmDelete(): void {
        if (!this.itemToDelete) return;

        this.proveedorService.deletePortafolio(this.itemToDelete.id).subscribe({
            next: () => {
                this.showDeleteDialog.set(false);
                this.itemToDelete = null;
                this.loadPortafolio();
            },
            error: () => {
                this.showDeleteDialog.set(false);
                this.itemToDelete = null;
            },
        });
    }

    protected cancelDelete(): void {
        this.showDeleteDialog.set(false);
        this.itemToDelete = null;
    }

    protected goBack(): void {
        this.router.navigate(['/proveedores-personas']);
    }

    private buildForm(): void {
        this.form = this.fb.group({
            servicioId: ['', [Validators.required]],
            precioUnitario: ['', [Validators.required, Validators.min(0)]],
        });
    }

    private loadServicios(): void {
        this.proveedorService.getServicios().subscribe({
            next: (servicios) => {
                this.servicios.set(servicios);
                this.serviciosMap.clear();
                servicios.forEach((s) => this.serviciosMap.set(s.id, s.nombre));
            },
        });
    }

    private loadPortafolio(): void {
        this.loading.set(true);
        this.proveedorService.getPortafolio(this.proveedorId).subscribe({
            next: (items) => {
                this.portafolio.set(items);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            },
        });
    }
}
