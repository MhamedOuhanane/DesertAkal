import { Component, computed, effect, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ReviewCard } from '../../../shared/components/review-card/review-card';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { AuthStore } from '../../../core/auth/auth.store';
import { GuideService } from '../../../core/services/guide-service';
import { Pagination } from '../../../core/models/response.models';
import { Review, ReviewFilters } from '../../../core/models/review.model';

@Component({
    selector: 'app-guide-reviews',
    standalone: true,
    imports: [MatIcon, ReviewCard, PaginationComponent],
    templateUrl: './guide-reviews.html',
})
export class GuideReviews {
    private authStore = inject(AuthStore);
    private guideService = inject(GuideService);

    userUuid = computed(() => this.authStore.user()?.uuid || '');

    pagination = signal<Pagination<Review> | null>(null);
    reviews = computed<Review[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<ReviewFilters>({
        page: 0,
        size: 10,
        sortBy: 'createdAt',
        order: 'desc',
    });

    averageRating = signal(0);

    constructor() {
        effect(() => {
            this.loadReviews();
        });
    }

    async loadReviews(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.guideService.getReviews(this.userUuid(), this.query()),
            );
            if (res.data) {
                this.pagination.set(res.data);
                this.calculateAverage();
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load reviews');
        } finally {
            this.isLoading.set(false);
        }
    }

    private calculateAverage(): void {
        const items = this.reviews();
        if (items.length === 0) {
            this.averageRating.set(0);
            return;
        }
        const sum = items.reduce((acc, r) => acc + r.rating, 0);
        this.averageRating.set(sum / items.length);
    }

    goToPage(page: number): void {
        this.query.update((q) => ({ ...q, page }));
        this.loadReviews();
    }

    get formattedRating(): string {
        return this.averageRating().toFixed(1);
    }

    get roundedRating(): number {
        return Math.round(this.averageRating());
    }
}
