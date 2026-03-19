import { Component, computed, effect, inject, signal } from '@angular/core';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { PaymentCard } from '../../../shared/components/payment-card/payment-card';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { AuthStore } from '../../../core/auth/auth.store';
import { TouristService } from '../../../core/services/tourist-service';
import { Pagination } from '../../../core/models/response.models';
import { Payment } from '../../../core/models/payment.model';

@Component({
    selector: 'app-my-payments',
    imports: [PaymentCard, PaginationComponent, MatIcon],
    templateUrl: './my-payments.html',
})
export class MyPayments {
    private authStore = inject(AuthStore);
    private touristService = inject(TouristService);

    userUuid = computed(() => this.authStore.user()?.uuid || '');

    pagination = signal<Pagination<Payment> | null>(null);
    payments = computed<Payment[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    statusFilter = signal('');
    currentPage = signal(0);

    readonly statuses = ['COMPLETED', 'PENDING', 'FAILED', 'REFUNDED'];

    constructor() {
        effect(() => {
            this.loadPayments();
        });
    }

    async loadPayments(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.touristService.getPayments(this.userUuid(), {
                    page: this.currentPage(),
                    size: 10,
                    status: this.statusFilter() || undefined,
                }),
            );
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load payments');
        } finally {
            this.isLoading.set(false);
        }
    }

    onStatusFilter(status: string): void {
        this.statusFilter.set(status);
        this.currentPage.set(0);
        this.loadPayments();
    }

    goToPage(page: number): void {
        this.currentPage.set(page);
        this.loadPayments();
    }
}
