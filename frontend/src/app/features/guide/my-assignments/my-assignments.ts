import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { AuthStore } from '../../../core/auth/auth.store';
import { GuideService } from '../../../core/services/guide-service';
import { Reservation, ReservationFilters } from '../../../core/models/reservation.model';
import { Pagination } from '../../../core/models/response.models';
import { ReservationCard } from '../../../shared/components/reservation-card/reservation-card';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-my-assignments',
    standalone: true,
    imports: [MatIcon, ReservationCard, PaginationComponent],
    templateUrl: './my-assignments.html',
})
export class MyAssignments {
    private authStore = inject(AuthStore);
    private guideService = inject(GuideService);
    private router = inject(Router);

    userUuid = computed(() => this.authStore.user()?.uuid || '');

    pagination = signal<Pagination<Reservation> | null>(null);
    assignments = computed<Reservation[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<ReservationFilters>({
        page: 0,
        size: 10,
        sortBy: 'startDate',
        order: 'asc',
    });

    statusFilter = signal('');

    readonly statuses = ['CONFIRMED', 'PENDING', 'COMPLETED', 'CANCELLED'];

    constructor() {
        effect(() => {
            this.loadAssignments();
        });
    }

    async loadAssignments(): Promise<void> {
        this.isLoading.set(true);
        try {
            const params: ReservationFilters = {
                ...this.query(),
                status: this.statusFilter() || undefined,
            };
            const res = await firstValueFrom(
                this.guideService.getReservations(this.userUuid(), params),
            );
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load assignments');
        } finally {
            this.isLoading.set(false);
        }
    }

    onStatusFilter(status: string): void {
        this.statusFilter.set(status);
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadAssignments();
    }

    goToPage(page: number): void {
        this.query.update((q) => ({ ...q, page }));
        this.loadAssignments();
    }

    viewAssignment(uuid: string): void {
        this.router.navigate(['/guide/dashboard/assignments', uuid]);
    }

    getStatusConfig(status: string): { bg: string; text: string; dot: string } {
        const map: Record<string, any> = {
            CONFIRMED: { bg: 'bg-green-500/10', text: 'text-green-600', dot: 'bg-green-500' },
            PENDING: { bg: 'bg-orange-500/10', text: 'text-orange-600', dot: 'bg-orange-500' },
            CANCELLED: { bg: 'bg-red-500/10', text: 'text-red-600', dot: 'bg-red-500' },
            COMPLETED: { bg: 'bg-blue-500/10', text: 'text-blue-600', dot: 'bg-blue-500' },
        };
        return map[status] || { bg: 'bg-gray-500/10', text: 'text-gray-500', dot: 'bg-gray-400' };
    }

    getDaysUntil(dateStr: string | Date): number {
        const date = new Date(dateStr);
        const now = new Date();
        return Math.ceil((date.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    }
}
