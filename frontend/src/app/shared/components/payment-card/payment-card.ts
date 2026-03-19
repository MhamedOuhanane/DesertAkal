import { Component, input } from '@angular/core';
import { Payment } from '../../../core/models/payment.model';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-payment-card',
    standalone: true,
    imports: [DatePipe, CurrencyPipe, MatIcon],
    template: `
        <div
            class="flex items-center gap-3 rounded-xl border border-border p-3 transition-all hover:border-primary/20"
        >
            <div
                class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl"
                [class]="getTypeConfig().bg"
            >
                <mat-icon style="font-size: 16px; width: 16px; height: 16px">
                    {{ getTypeConfig().icon }}
                </mat-icon>
            </div>
            <div class="min-w-0 flex-1">
                <p class="text-xs font-bold text-text-primary">
                    {{ payment().amount | currency: 'MAD ' : 'symbol' : '1.2-2' }}
                </p>
                <p class="text-[10px] text-text-tertiary">
                    {{ payment().date | date: 'MMM d, y HH:mm' }} · {{ payment().method }}
                </p>
            </div>
            <span
                class="rounded-full px-2 py-0.5 text-[9px] font-semibold"
                [class]="getStatusColor()"
            >
                {{ payment().status }}
            </span>
        </div>
    `,
})
export class PaymentCard {
    payment = input.required<Payment>();

    getTypeConfig(): { bg: string; icon: string } {
        return this.payment().type === 'REFUND'
            ? { bg: 'bg-blue-500/10 text-blue-600', icon: 'undo' }
            : { bg: 'bg-green-500/10 text-green-600', icon: 'payment' };
    }

    getStatusColor(): string {
        const map: Record<string, string> = {
            COMPLETED: 'bg-green-500/10 text-green-600',
            PENDING: 'bg-orange-500/10 text-orange-600',
            FAILED: 'bg-red-500/10 text-red-600',
            REFUNDED: 'bg-blue-500/10 text-blue-600',
        };
        return map[this.payment().status] || 'bg-gray-500/10 text-gray-500';
    }
}
