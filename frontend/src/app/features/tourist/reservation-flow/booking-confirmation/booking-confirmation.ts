import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ReservationFind } from '../../../../core/models/reservation.model';
import { ReservationService } from '../../../../core/services/reservation-service';

@Component({
    selector: 'app-booking-confirmation',
    imports: [RouterLink, DatePipe, CurrencyPipe, MatIcon],
    templateUrl: './booking-confirmation.html',
})
export class BookingConfirmation implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private reservationService = inject(ReservationService);

    reservation = signal<ReservationFind | null>(null);
    isLoading = signal(true);
    isDownloading = signal(false);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/tourist/dashboard/bookings']);
            return;
        }
        await this.loadReservation(uuid);
    }

    private async loadReservation(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.reservationService.findOne(uuid));
            this.reservation.set(res.data!);
        } catch {
            toast.error('Reservation not found');
            this.router.navigate(['/tourist/dashboard/bookings']);
        } finally {
            this.isLoading.set(false);
        }
    }

    async downloadPdf(): Promise<void> {
        const r = this.reservation();
        if (!r) return;

        this.isDownloading.set(true);
        try {
            const blob = await firstValueFrom(this.reservationService.downloadPdf(r.uuid));
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `Voucher_${r.reference}.pdf`;
            a.click();
            window.URL.revokeObjectURL(url);
            toast.success('PDF downloaded!');
        } catch {
            toast.error('Failed to download PDF');
        } finally {
            this.isDownloading.set(false);
        }
    }

    getStatusConfig(status: string): { bg: string; text: string; icon: string; label: string } {
        const map: Record<string, any> = {
            CONFIRMED: {
                bg: 'bg-green-500/10',
                text: 'text-green-600',
                icon: 'check_circle',
                label: 'Confirmed',
            },
            PENDING: {
                bg: 'bg-orange-500/10',
                text: 'text-orange-600',
                icon: 'schedule',
                label: 'Pending Payment',
            },
            CANCELLED: {
                bg: 'bg-red-500/10',
                text: 'text-red-600',
                icon: 'cancel',
                label: 'Cancelled',
            },
            COMPLETED: {
                bg: 'bg-blue-500/10',
                text: 'text-blue-600',
                icon: 'task_alt',
                label: 'Completed',
            },
        };
        return (
            map[status] || {
                bg: 'bg-gray-500/10',
                text: 'text-gray-500',
                icon: 'help',
                label: status,
            }
        );
    }
}
