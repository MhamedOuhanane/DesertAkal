import { Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { Payment, PaymentFilters } from '../../../../core/models/payment.model';
import { Pagination } from '../../../../core/models/response.models';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { PaymentService } from '../../../../core/services/payment-service';
import { RefundDialog } from '../../../../shared/components/refund-dialog/refund-dialog';

@Component({
    selector: 'app-payment-list',
    standalone: true,
    imports: [
        RouterLink,
        FormsModule,
        DatePipe,
        CurrencyPipe,
        MatIcon,
        PaginationComponent,
        RefundDialog,
    ],
    templateUrl: './payment-list.html',
})
export class PaymentList {
    private paymentService = inject(PaymentService);

    pagination = signal<Pagination<Payment> | null>(null);
    payments = computed<Payment[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<PaymentFilters>({
        page: 0,
        size: 10,
        sortBy: 'date',
        order: 'desc',
    });

    statusFilter = signal('');
    typeFilter = signal('');
    methodFilter = signal('');

    expandedPayment = signal<string | null>(null);

    showRefundDialog = signal(false);
    paymentToRefund = signal<Payment | null>(null);
    isRefunding = signal(false);

    constructor() {
        effect(() => {
            this.loadPayments();
        });
    }

    async loadPayments(): Promise<void> {
        this.isLoading.set(true);
        try {
            const params: PaymentFilters = {
                ...this.query(),
                status: this.statusFilter() || undefined,
                type: this.typeFilter() || undefined,
                method: this.methodFilter() || undefined,
            };
            const res = await firstValueFrom(this.paymentService.findAll(params));
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load payments');
        } finally {
            this.isLoading.set(false);
        }
    }

    onStatusFilter(v: string): void {
        this.statusFilter.set(v);
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadPayments();
    }

    onTypeFilter(v: string): void {
        this.typeFilter.set(v);
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadPayments();
    }

    onMethodFilter(v: string): void {
        this.methodFilter.set(v);
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadPayments();
    }

    clearFilters(): void {
        this.statusFilter.set('');
        this.typeFilter.set('');
        this.methodFilter.set('');
        this.query.update((q) => ({ ...q, page: 0 }));
        this.loadPayments();
    }

    get hasActiveFilters(): boolean {
        return !!(this.statusFilter() || this.typeFilter() || this.methodFilter());
    }

    onSort(field: string): void {
        const { sortBy, order } = this.query();
        this.query.update((q) => ({
            ...q,
            sortBy: field,
            order: sortBy === field && order === 'asc' ? 'desc' : 'asc',
        }));
        this.loadPayments();
    }

    goToPage(page: number): void {
        this.query.update((q) => ({ ...q, page }));
        this.loadPayments();
    }

    toggleExpand(uuid: string): void {
        this.expandedPayment.update((c) => (c === uuid ? null : uuid));
    }

    openRefundDialog(payment: Payment, event: Event): void {
        event.stopPropagation();
        this.paymentToRefund.set(payment);
        this.showRefundDialog.set(true);
    }

    async onRefundConfirm(data: { type: 'full' | 'partial'; amount?: number }): Promise<void> {
        const payment = this.paymentToRefund();
        if (!payment) return;

        this.isRefunding.set(true);
        try {
            if (data.type === 'full') {
                await firstValueFrom(this.paymentService.refund(payment.uuid));
                toast.success('Full refund processed');
            } else {
                await firstValueFrom(
                    this.paymentService.partialRefund(payment.uuid, {
                        amount: data.amount!,
                    }),
                );
                toast.success(`Partial refund of ${data.amount} MAD processed`);
            }
            this.showRefundDialog.set(false);
            this.paymentToRefund.set(null);
            await this.loadPayments();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Refund failed');
        } finally {
            this.isRefunding.set(false);
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
            COMPLETED: {
                bg: 'bg-green-500/10',
                text: 'text-green-600',
                icon: 'check_circle',
            },
            PENDING: {
                bg: 'bg-orange-500/10',
                text: 'text-orange-600',
                icon: 'schedule',
            },
            FAILED: {
                bg: 'bg-red-500/10',
                text: 'text-red-600',
                icon: 'error',
            },
            CANCELED: {
                bg: 'bg-gray-500/10',
                text: 'text-gray-500',
                icon: 'cancel',
            },
            REFUNDED: {
                bg: 'bg-blue-500/10',
                text: 'text-blue-600',
                icon: 'replay',
            },
            REFUNDED_PARTIAL: {
                bg: 'bg-indigo-500/10',
                text: 'text-indigo-600',
                icon: 'sync',
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

    getTypeConfig(type: string): { bg: string; icon: string } {
        if (type === 'REFUND') {
            return { bg: 'bg-blue-500/10 text-blue-600', icon: 'undo' };
        }
        return { bg: 'bg-green-500/10 text-green-600', icon: 'payment' };
    }

    getMethodIcon(method: string): string {
        const map: Record<string, string> = {
            PAYPAL: 'account_balance_wallet',
            STRIPE: 'credit_card',
            CASH: 'payments',
        };
        return map[method] || 'payment';
    }

    canRefund(payment: Payment): boolean {
        return (
            (payment.status === 'COMPLETED' || payment.status === 'REFUNDED_PARTIAL') &&
            payment.type !== 'REFUND'
        );
    }

    readonly statuses = [
        'COMPLETED',
        'PENDING',
        'FAILED',
        'CANCELED',
        'REFUNDED',
        'REFUNDED_PARTIAL',
    ];
    readonly types = ['PAYMENT', 'REFUND'];
    readonly methods = ['PAYPAL', 'STRIPE'];
}
