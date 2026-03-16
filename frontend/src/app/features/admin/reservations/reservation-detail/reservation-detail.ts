import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ReservationFind } from '../../../../core/models/reservation.model';
import { Payment } from '../../../../core/models/payment.model';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { ReservationService } from '../../../../core/services/reservation-service';

@Component({
    selector: 'app-reservation-detail',
    imports: [RouterLink, DatePipe, CurrencyPipe, MatIcon, DeleteDialog],
    templateUrl: './reservation-detail.html',
})
export class ReservationDetail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private reservationService = inject(ReservationService);

    reservation = signal<ReservationFind | null>(null);
    isLoading = signal(true);

    payments = signal<Payment[]>([]);
    paymentsLoading = signal(false);
    paymentsTotalElements = signal(0);

    showCancelDialog = signal(false);
    isCancelling = signal(false);
    showDeleteDialog = signal(false);
    isDeleting = signal(false);
    isDownloading = signal(false);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/dashboard/reservations']);
            return;
        }
        await this.loadReservation(uuid);
    }

    private async loadReservation(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.reservationService.findOne(uuid));
            this.reservation.set(res.data!);
            await this.loadPayments();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Reservation not found');
            this.router.navigate(['/dashboard/reservations']);
        } finally {
            this.isLoading.set(false);
        }
    }

    async loadPayments(): Promise<void> {
        if (!this.reservation()) return;
        this.paymentsLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.reservationService.getPayments(this.reservation()!.uuid, {
                    page: 0,
                    size: 20,
                }),
            );
            this.payments.set((res.data?.content as Payment[]) || []);
            this.paymentsTotalElements.set(res.data?.totalElements || 0);
        } catch {
        } finally {
            this.paymentsLoading.set(false);
        }
    }

    async downloadPdf(): Promise<void> {
        if (!this.reservation()) return;
        this.isDownloading.set(true);
        try {
            const blob = await firstValueFrom(
                this.reservationService.downloadPdf(this.reservation()!.uuid),
            );
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `Voucher_${this.reservation()!.reference}.pdf`;
            a.click();
            window.URL.revokeObjectURL(url);
            toast.success('PDF downloaded');
        } catch {
            toast.error('Failed to download PDF');
        } finally {
            this.isDownloading.set(false);
        }
    }

    async confirmCancel(): Promise<void> {
        if (!this.reservation()) return;
        this.isCancelling.set(true);
        try {
            await firstValueFrom(this.reservationService.cancel(this.reservation()!.uuid));
            toast.success('Reservation cancelled');
            this.showCancelDialog.set(false);
            await this.loadReservation(this.reservation()!.uuid);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to cancel');
        } finally {
            this.isCancelling.set(false);
        }
    }

    async confirmDelete(): Promise<void> {
        if (!this.reservation()) return;
        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.reservationService.delete(this.reservation()!.uuid));
            toast.success('Reservation deleted');
            this.router.navigate(['/dashboard/reservations']);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete');
        } finally {
            this.isDeleting.set(false);
            this.showDeleteDialog.set(false);
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

    getStatusConfig(status: string): { bg: string; text: string; icon: string } {
        const map: Record<string, any> = {
            CONFIRMED: { bg: 'bg-green-500/10', text: 'text-green-600', icon: 'check_circle' },
            PENDING: { bg: 'bg-orange-500/10', text: 'text-orange-600', icon: 'schedule' },
            CANCELLED: { bg: 'bg-red-500/10', text: 'text-red-600', icon: 'cancel' },
            COMPLETED: { bg: 'bg-blue-500/10', text: 'text-blue-600', icon: 'task_alt' },
            REJECTED: { bg: 'bg-gray-500/10', text: 'text-gray-500', icon: 'do_not_disturb' },
        };
        return map[status] || { bg: 'bg-gray-500/10', text: 'text-gray-500', icon: 'help' };
    }

    get canCancel(): boolean {
        const s = this.reservation()?.status;
        return s === 'PENDING' || s === 'CONFIRMED';
    }

    getPaymentStatusColor(status: string): string {
        switch (status) {
            case 'COMPLETED':
                return 'bg-green-500/10 text-green-600';
            case 'PENDING':
                return 'bg-orange-500/10 text-orange-600';
            case 'FAILED':
                return 'bg-red-500/10 text-red-600';
            case 'REFUNDED':
                return 'bg-blue-500/10 text-blue-600';
            default:
                return 'bg-gray-500/10 text-gray-500';
        }
    }
}
