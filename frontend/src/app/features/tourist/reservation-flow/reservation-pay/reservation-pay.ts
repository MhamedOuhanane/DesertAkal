import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ReservationService } from '../../../../core/services/reservation-service';
import { PaymentService } from '../../../../core/services/payment-service';
import { ReservationFind } from '../../../../core/models/reservation.model';
import { Payment, PaymentResponse } from '../../../../core/models/payment.model';
import { PaymentMethod } from '../../../../core/enums/payment-method.enum';
import { PaymentCard } from '../../../../shared/components/payment-card/payment-card';

@Component({
    selector: 'app-booking-pay',
    imports: [RouterLink, DatePipe, CurrencyPipe, MatIcon, PaymentCard],
    templateUrl: './reservation-pay.html',
})
export class ReservationPay implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private reservationService = inject(ReservationService);
    private paymentService = inject(PaymentService);

    PaymentMethod = PaymentMethod;

    reservation = signal<ReservationFind | null>(null);
    payments = signal<Payment[]>([]);
    isLoading = signal(true);
    isProcessing = signal(false);
    selectedMethod = signal<PaymentMethod>(PaymentMethod.PAYPAL);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/tourist/dashboard/bookings']);
            return;
        }
        await this.loadReservation(uuid);
        await this.loadPayments(uuid);
        this.isLoading.set(false);
    }

    private async loadReservation(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.reservationService.findOne(uuid));
            this.reservation.set(res.data!);

            if (res.data!.status === 'CONFIRMED') {
                this.router.navigate(['/tourist/dashboard/bookings', uuid, 'confirmation']);
                return;
            }

            if (res.data!.status !== 'PENDING') {
                toast.error('This reservation cannot be paid');
                this.router.navigate(['/tourist/dashboard/bookings']);
            }
        } catch {
            toast.error('Reservation not found');
            this.router.navigate(['/tourist/dashboard/bookings']);
        }
    }

    private async loadPayments(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.reservationService.getPayments(uuid, { page: 0, size: 10 }),
            );
            this.payments.set((res.data?.content as Payment[]) || []);
        } catch {}
    }

    hasPendingPayment(): boolean {
        return this.payments().some((p) => p.status === 'PENDING');
    }

    async initiatePayment(): Promise<void> {
        const r = this.reservation();
        if (!r) return;

        if (this.hasPendingPayment()) {
            toast.error('You already have a pending payment. Please complete or cancel it first.');
            return;
        }

        this.isProcessing.set(true);
        try {
            const dto = {
                reservationUuid: r.uuid,
                method: this.selectedMethod(),
            };
            const res = await firstValueFrom(this.paymentService.initiate(dto));
            const data = res.data as PaymentResponse;

            if (data.approvalUrl) {
                window.location.href = data.approvalUrl;
            } else {
                toast.error('No approval URL received');
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Payment initiation failed');
        } finally {
            this.isProcessing.set(false);
        }
    }

    async cancelPendingPayment(): Promise<void> {
        const pending = this.payments().find((p) => p.status === 'PENDING');
        if (!pending) return;

        try {
            await firstValueFrom(this.paymentService.cancel(pending.uuid));
            toast.success('Pending payment cancelled');
            await this.loadPayments(this.reservation()!.uuid);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to cancel payment');
        }
    }

    getStatusColor(status: string): string {
        const map: Record<string, string> = {
            COMPLETED: 'bg-green-500/10 text-green-600',
            PENDING: 'bg-orange-500/10 text-orange-600',
            FAILED: 'bg-red-500/10 text-red-600',
            CANCELED: 'bg-gray-500/10 text-gray-500',
        };
        return map[status] || 'bg-gray-500/10 text-gray-500';
    }
}
