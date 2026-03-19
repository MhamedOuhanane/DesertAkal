import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { AuthStore } from '../../../core/auth/auth.store';
import { NavigationService } from '../../../core/services/navigation-service';
import { ReservationFind } from '../../../core/models/reservation.model';
import { Payment } from '../../../core/models/payment.model';
import { RoleEnum } from '../../../core/enums/role.enum';
import { PaymentCard } from '../../components/payment-card/payment-card';
import { DeleteDialog } from '../../components/delete-dialog/delete-dialog';
import { HasRole } from '../../directives';
import { ReservationService } from '../../../core/services/reservation-service';

@Component({
    selector: 'app-reservation-detail',
    standalone: true,
    imports: [RouterLink, DatePipe, CurrencyPipe, MatIcon, HasRole, PaymentCard, DeleteDialog],
    templateUrl: './reservation-detail.html',
})
export class ReservationDetail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private reservationService = inject(ReservationService);
    private authStore = inject(AuthStore);
    private navService = inject(NavigationService);

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

    role = computed(() => this.authStore.userRole());
    isAdmin = computed(() => this.role() === RoleEnum.ADMIN);
    isGuide = computed(() => this.role() === RoleEnum.GUIDE);
    isTourist = computed(() => this.role() === RoleEnum.TOURIST);

    backLink = computed(() => {
        switch (this.role()) {
            case RoleEnum.ADMIN:
                return '/dashboard/reservations';
            case RoleEnum.GUIDE:
                return '/guide/dashboard/assignments';
            case RoleEnum.TOURIST:
                return '/tourist/dashboard/bookings';
            default:
                return '/';
        }
    });

    pageTitle = computed(() => {
        switch (this.role()) {
            case RoleEnum.ADMIN:
                return 'Reservation';
            case RoleEnum.GUIDE:
                return 'Assignment';
            case RoleEnum.TOURIST:
                return 'My Booking';
            default:
                return 'Reservation';
        }
    });

    tourLink = computed(() => {
        if (this.isAdmin()) return '/dashboard/tours';
        return '/tours';
    });

    touristProfileLink = computed(() => (this.isAdmin() ? '/dashboard/users' : null));
    guideProfileLink = computed(() => (this.isAdmin() ? '/dashboard/guides' : null));

    protected readonly RoleEnum = RoleEnum;

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate([this.backLink()]);
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
            this.router.navigate([this.backLink()]);
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
            toast.success('Voucher downloaded');
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
            this.router.navigate([this.backLink()]);
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

    getStatusConfig(status: string): {
        bg: string;
        text: string;
        icon: string;
    } {
        const map: Record<string, any> = {
            CONFIRMED: {
                bg: 'bg-green-500/10',
                text: 'text-green-600',
                icon: 'check_circle',
            },
            PENDING: {
                bg: 'bg-orange-500/10',
                text: 'text-orange-600',
                icon: 'schedule',
            },
            CANCELLED: {
                bg: 'bg-red-500/10',
                text: 'text-red-600',
                icon: 'cancel',
            },
            COMPLETED: {
                bg: 'bg-blue-500/10',
                text: 'text-blue-600',
                icon: 'task_alt',
            },
            REJECTED: {
                bg: 'bg-gray-500/10',
                text: 'text-gray-500',
                icon: 'do_not_disturb',
            },
        };
        return (
            map[status] || {
                bg: 'bg-gray-500/10',
                text: 'text-gray-500',
                icon: 'help',
            }
        );
    }

    get canCancel(): boolean {
        const s = this.reservation()?.status;
        return s === 'PENDING' || s === 'CONFIRMED';
    }

    getPaymentStatusColor(status: string): string {
        const map: Record<string, string> = {
            COMPLETED: 'bg-green-500/10 text-green-600',
            PENDING: 'bg-orange-500/10 text-orange-600',
            FAILED: 'bg-red-500/10 text-red-600',
            REFUNDED: 'bg-blue-500/10 text-blue-600',
        };
        return map[status] || 'bg-gray-500/10 text-gray-500';
    }
}
