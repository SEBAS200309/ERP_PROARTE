import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AnimatedButtonComponent } from '../../../shared/components/animated-button/animated-button.component';
import { RolService } from '../rol.service';
import { Rol } from '../rol.models';

@Component({
    selector: 'app-rol-detail',
    standalone: true,
    imports: [CommonModule, AnimatedButtonComponent],
    templateUrl: './rol-detail.component.html',
    styleUrl: './rol-detail.component.scss',
})
export class RolDetailComponent implements OnInit {
    private readonly rolService = inject(RolService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    protected readonly loading = signal(true);
    protected readonly rol = signal<Rol | null>(null);

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadRol(id);
        } else {
            this.router.navigate(['/roles']);
        }
    }

    protected goBack(): void {
        this.router.navigate(['/roles']);
    }

    private loadRol(id: string): void {
        this.loading.set(true);
        this.rolService.getById(id).subscribe({
            next: (data) => {
                this.rol.set(data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.router.navigate(['/roles']);
            },
        });
    }
}