import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { Reservation, ReservationFilters } from '../../../../core/models/reservation.model';
import { Pagination } from '../../../../core/models/response.models';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { ReservationService } from '../../../../core/services/reservation-service';

@Component({
    selector: 'app-reservation-list',
    imports: [FormsModule, DatePipe, CurrencyPipe, MatIcon, PaginationComponent, DeleteDialog],
    templateUrl: './reservation-list.html',
})
export class ReservationList {
    private reservationService = inject(ReservationService);
    private router = inject(Router);

    pagination = signal<Pagination<Reservation> | null>(null);
    reservations = computed<Reservation[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<ReservationFilters>({
        page: 0,
        size: 10,
        sortBy: 'createdAt',
        order: 'desc',
    });

    statusFilter = signal<string>('');
    tourSearch = signal('');
    touristSearch = signal('');
    startDateFilter = signal('');
    endDateFilter = signal('');
    showFilters = signal(false);

    showCancelDialog = signal(false);
    reservationToCancel = signal<Reservation | null>(null);
    isCancelling = signal(false);

    showDeleteDialog = signal(false);
    reservationToDelete = signal<Reservation | null>(null);
    isDeleting = signal(false);

    referenceSearch = signal('');

    private tourSubject = new Subject<string>();
    private touristSubject = new Subject<string>();

    constructor() {
        this.tourSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((tour) => {
                this.tourSearch.set(tour);
                this.query.update((q) => ({ ...q, page: 0 }));
                this.loadReservations();
            });

        this.touristSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((tourist) => {
                this.touristSearch.set(tourist);
                this.query.update((q) => ({ ...q, page: 0 }));
                this.loadReservations();
            });

        effect(() => {
            this.loadReservations();
        });
    }

    async loadReservations(): Promise<void> {
        this.isLoading.set(true);
        try {
            const params: ReservationFilters = {
                ...this.query(),
                tour: this.tourSearch() || undefined,
                tourist: this.touristSearch() || undefined,
                status: this.statusFilter() || undefined,
                startDate: this.startDateFilter()
                    ? this.startDateFilter() + 'T00:00:00'
                    : undefined,
                endDate: this.endDateFilter() ? this.endDateFilter() + 'T23:59:59' : undefined,
            };
            const res = await firstValueFrom(this.reservationService.findAll(params));
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load reservations');
        } finally {
            this.isLoading.set(false);
        }
    }

    onTourSearch(v: string): void {
        this.tourSubject.next(v);
    }
    onTouristSearch(v: string): void {
        this.touristSubject.next(v);
    }

    onStatusFilter(status: string): void {
        this.statusFilter.set(status);
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadReservations();
    }

    onDateFilter(field: 'start' | 'end', value: string): void {
        if (field === 'start') this.startDateFilter.set(value);
        else this.endDateFilter.set(value);
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadReservations();
    }

    clearFilters(): void {
        this.statusFilter.set('');
        this.tourSearch.set('');
        this.touristSearch.set('');
        this.startDateFilter.set('');
        this.endDateFilter.set('');
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadReservations();
    }

    get hasActiveFilters(): boolean {
        return !!(
            this.statusFilter() ||
            this.tourSearch() ||
            this.touristSearch() ||
            this.startDateFilter() ||
            this.endDateFilter()
        );
    }

    onSort(field: string): void {
        const { sortBy, order } = this.query();
        this.query.update((q) => ({
            ...q,
            sortBy: field,
            order: sortBy === field && order === 'asc' ? 'desc' : 'asc',
        }));
        this.loadReservations();
    }

    goToPage(page: number): void {
        this.query.update((q) => ({ ...q, page }));
        this.loadReservations();
    }

    viewReservation(uuid: string): void {
        this.router.navigate(['/dashboard/reservations', uuid]);
    }

    openCancelDialog(r: Reservation, e: Event): void {
        e.stopPropagation();
        this.reservationToCancel.set(r);
        this.showCancelDialog.set(true);
    }

    async confirmCancel(): Promise<void> {
        const r = this.reservationToCancel();
        if (!r) return;
        this.isCancelling.set(true);
        try {
            await firstValueFrom(this.reservationService.cancel(r.uuid));
            toast.success('Reservation cancelled');
            this.showCancelDialog.set(false);
            this.reservationToCancel.set(null);
            await this.loadReservations();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to cancel');
        } finally {
            this.isCancelling.set(false);
        }
    }

    openDeleteDialog(r: Reservation, e: Event): void {
        e.stopPropagation();
        this.reservationToDelete.set(r);
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        const r = this.reservationToDelete();
        if (!r) return;
        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.reservationService.delete(r.uuid));
            toast.success('Reservation deleted');
            this.showDeleteDialog.set(false);
            this.reservationToDelete.set(null);
            await this.loadReservations();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete');
        } finally {
            this.isDeleting.set(false);
        }
    }

    getInitials(name: string): string {
        return (
            name
                ?.split(' ')
                .map((w) => w.charAt(0))
                .join('')
                .toUpperCase()
                .slice(0, 2) || '?'
        );
    }

    getStatusConfig(status: string): { bg: string; text: string; dot: string; icon: string } {
        const map: Record<string, any> = {
            CONFIRMED: {
                bg: 'bg-green-500/10',
                text: 'text-green-600',
                dot: 'bg-green-500',
                icon: 'check_circle',
            },
            PENDING: {
                bg: 'bg-orange-500/10',
                text: 'text-orange-600',
                dot: 'bg-orange-500',
                icon: 'schedule',
            },
            CANCELLED: {
                bg: 'bg-red-500/10',
                text: 'text-red-600',
                dot: 'bg-red-500',
                icon: 'cancel',
            },
            COMPLETED: {
                bg: 'bg-blue-500/10',
                text: 'text-blue-600',
                dot: 'bg-blue-500',
                icon: 'task_alt',
            },
            REJECTED: {
                bg: 'bg-gray-500/10',
                text: 'text-gray-500',
                dot: 'bg-gray-400',
                icon: 'do_not_disturb',
            },
        };
        return (
            map[status] || {
                bg: 'bg-gray-500/10',
                text: 'text-gray-500',
                dot: 'bg-gray-400',
                icon: 'help',
            }
        );
    }

    readonly statuses: string[] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'REJECTED', 'COMPLETED'];
}
