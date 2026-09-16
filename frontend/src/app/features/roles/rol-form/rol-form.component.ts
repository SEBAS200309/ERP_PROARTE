import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { RolService } from '../rol.service';

const ACCIONES = ['ver_listado', 'ver_detalle', 'crear', 'editar', 'eliminar'] as const;

interface ContextoEntry {
    tabla: string;
    values: string;
}

@Component({
    selector: 'app-rol-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, AnimatedButtonComponent],
    templateUrl: './rol-form.component.html',
    styleUrl: './rol-form.component.scss',
})
export class RolFormComponent implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly rolService = inject(RolService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    protected readonly loading = signal(false);
    protected readonly saving = signal(false);
    protected readonly isEditMode = signal(false);
    protected readonly pageTitle = computed(() =>
        this.isEditMode() ? 'Editar Rol y Permisos' : 'Nuevo Rol'
    );

    protected form!: FormGroup;
    private rolId: string | null = null;

    // Lógica de Permisos y Contexto heredada del permiso-editor
    protected readonly tablas = signal<string[]>([]);
    protected readonly acciones = ACCIONES;
    protected readonly accionLabels: Record<string, string> = {
        ver_listado: 'Ver Listado',
        ver_detalle: 'Ver Detalle',
        crear: 'Crear',
        editar: 'Editar',
        eliminar: 'Eliminar',
    };
    protected permisos: Record<string, Record<string, boolean>> = {};
    protected contexto = signal<ContextoEntry[]>([]);

    ngOnInit(): void {
        this.buildForm();
        this.rolId = this.route.snapshot.paramMap.get('id');
        if (this.rolId) {
            this.isEditMode.set(true);
            this.loadRol(this.rolId);
        }
    }

    private buildForm(): void {
        this.form = this.fb.group({
            nombre: ['', [Validators.required, Validators.minLength(3)]],
            descripcion: ['', [Validators.required]],
        });
    }

    private loadRol(id: string): void {
        this.loading.set(true);
        this.rolService.getById(id).subscribe({
            next: (rol: any) => {
                this.form.patchValue({
                    nombre: rol.nombre,
                    descripcion: rol.descripcion,
                });

                // Cargar matriz de permisos
                this.permisos = rol.configuracion || {};

                // Extraer y limpiar contexto si se guarda dentro de la misma configuración JSONB
                if (this.permisos['contexto']) {
                    const contextoData = this.permisos['contexto'] as any;
                    const parsedContexto = Object.keys(contextoData).map(k => ({
                        tabla: k,
                        values: contextoData[k] as string
                    }));
                    this.contexto.set(parsedContexto);
                    delete this.permisos['contexto'];
                }

                this.tablas.set(Object.keys(this.permisos));
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.router.navigate(['/roles']);
            },
        });
    }

    // --- Lógica de Matriz Dinámica ---
    protected getPermiso(tabla: string, accion: string): boolean {
        return this.permisos[tabla]?.[accion] ?? false;
    }

    protected togglePermiso(tabla: string, accion: string): void {
        if (!this.permisos[tabla]) {
            this.permisos[tabla] = {};
        }
        this.permisos[tabla][accion] = !this.permisos[tabla][accion];
    }

    protected addTabla(): void {
        const newTabla = `tabla_${this.tablas().length + 1}`;
        this.tablas.update((t) => [...t, newTabla]);
        this.permisos[newTabla] = {
            ver_listado: false, ver_detalle: false, crear: false, editar: false, eliminar: false,
        };
    }

    // --- Lógica de Contexto ---
    protected addContextoEntry(): void {
        this.contexto.update((c) => [...c, { tabla: '', values: '' }]);
    }

    protected removeContextoEntry(index: number): void {
        this.contexto.update((c) => c.filter((_, i) => i !== index));
    }

    protected updateContextoTabla(index: number, value: string): void {
        this.contexto.update((c) =>
            c.map((entry, i) => (i === index ? { ...entry, tabla: value } : entry))
        );
    }

    protected updateContextoValues(index: number, value: string): void {
        this.contexto.update((c) =>
            c.map((entry, i) => (i === index ? { ...entry, values: value } : entry))
        );
    }

    // --- Guardado General ---
    protected save(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.saving.set(true);

        // Empaquetar configuración y contexto
        const finalConfiguracion: Record<string, any> = { ...this.permisos };
        if (this.contexto().length > 0) {
            finalConfiguracion['contexto'] = this.contexto().reduce((acc, curr) => {
                if (curr.tabla) acc[curr.tabla] = curr.values;
                return acc;
            }, {} as Record<string, string>);
        }

        const payload = {
            ...this.form.getRawValue(),
            configuracion: finalConfiguracion
        };

        const request$ = this.isEditMode() && this.rolId
            ? this.rolService.update(this.rolId, payload)
            : this.rolService.create(payload);

        request$.subscribe({
            next: () => {
                this.saving.set(false);
                this.router.navigate(['/roles']);
            },
            error: () => {
                this.saving.set(false);
            },
        });
    }

    protected cancel(): void {
        this.router.navigate(['/roles']);
    }

    protected hasError(field: string, error: string): boolean {
        const control = this.form.get(field);
        return !!control && control.hasError(error) && control.touched;
    }
}