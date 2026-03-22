import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { PaymentService } from '../../../../core/services/payment-service';
import { PaymentFind } from '../../../../core/models/payment.model';

@Component({
    selector: 'app-payment-callback',
    imports: [RouterLink, MatIcon],
    templateUrl: './payment-callback.html',
})
export class PaymentCallback implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private paymentService = inject(PaymentService);

    status = signal<'loading' | 'success' | 'error' | 'cancelled'>('loading');
    errorMessage = signal('');

    async ngOnInit(): Promise<void> {
        const params = this.route.snapshot.queryParamMap;

        const paypalToken = params.get('token');
        const cancelled = params.get('cancelled');

        if (cancelled === 'true') {
            this.status.set('cancelled');
            return;
        }

        if (!paypalToken) {
            this.status.set('error');
            this.errorMessage.set('No payment token found.');
            return;
        }

        await this.capturePayment(paypalToken);
    }

    private async capturePayment(orderId: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.paymentService.capture(orderId));
            const payment = res.data as PaymentFind;

            this.status.set('success');

            setTimeout(() => {
                this.router.navigate([
                    '/tourist/dashboard/bookings',
                    payment.reservation.uuid,
                    'confirmation',
                ]);
            }, 2000);
        } catch (err: any) {
            this.status.set('error');
            this.errorMessage.set(
                err?.error?.message || 'Payment capture failed. Please contact support.',
            );
        }
    }
}
