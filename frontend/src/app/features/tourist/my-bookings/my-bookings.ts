import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { AuthStore } from '../../../core/auth/auth.store';
import { TouristService } from '../../../core/services/tourist-service';
import { ReservationService } from '../../../core/services/reservation-service';
import { ReservationCard } from '../../../shared/components/reservation-card/reservation-card';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../shared/components/delete-dialog/delete-dialog';
import { Pagination } from '../../../core/models/response.models';
import { Reservation, ReservationFilters } from '../../../core/models/reservation.model';

@Component({
    selector: 'app-my-bookings',
    imports: [RouterLink, FormsModule, MatIcon, ReservationCard, PaginationComponent, DeleteDialog],
    templateUrl: './my-bookings.html',
})
export class MyBookings {
    private authStore = inject(AuthStore);
    private touristService = inject(TouristService);
    private reservationService = inject(ReservationService);
    private router = inject(Router);

    userUuid = computed(() => this.authStore.user()?.uuid || '');

    pagination = signal<Pagination<Reservation> | null>(null);
    bookings = computed<Reservation[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<ReservationFilters>({
        page: 0,
        size: 10,
        sortBy: 'createdAt',
        order: 'desc',
    });

    statusFilter = signal('');

    showCancelDialog = signal(false);
    bookingToCancel = signal<Reservation | null>(null);
    isCancelling = signal(false);

    readonly statuses = ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'];

    constructor() {
        effect(() => {
            this.loadBookings();
        });
    }

    async loadBookings(): Promise<void> {
        this.isLoading.set(true);
        try {
            const params: any = {
                ...this.query(),
                status: this.statusFilter() || undefined,
            };
            const res = await firstValueFrom(
                this.touristService.getReservations(this.userUuid(), params),
            );
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load bookings');
        } finally {
            this.isLoading.set(false);
        }
    }

    onStatusFilter(status: string): void {
        this.statusFilter.set(status);
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadBookings();
    }

    goToPage(page: number): void {
        this.query.update((q) => ({ ...q, page }));
        this.loadBookings();
    }

    viewBooking(uuid: string): void {
        this.router.navigate(['/tourist/dashboard/bookings', uuid]);
    }

    payBooking(uuid: string): void {
        this.router.navigate(['/tourist/dashboard/bookings', uuid]);
    }

    openCancelDialog(uuid: string): void {
        const booking = this.bookings().find((b) => b.uuid === uuid);
        if (booking) {
            this.bookingToCancel.set(booking);
            this.showCancelDialog.set(true);
        }
    }

    async confirmCancel(): Promise<void> {
        const booking = this.bookingToCancel();
        if (!booking) return;

        this.isCancelling.set(true);
        try {
            await firstValueFrom(this.reservationService.cancel(booking.uuid));
            toast.success('Booking cancelled successfully');
            this.showCancelDialog.set(false);
            this.bookingToCancel.set(null);
            await this.loadBookings();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to cancel');
        } finally {
            this.isCancelling.set(false);
        }
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
}
