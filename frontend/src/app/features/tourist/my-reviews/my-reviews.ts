import { Component, computed, effect, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { AuthStore } from '../../../core/auth/auth.store';
import { TouristService } from '../../../core/services/tourist-service';
import { ReviewService } from '../../../core/services/review-service';
import { Review } from '../../../core/models/review.model';
import { Pagination } from '../../../core/models/response.models';
import { ReviewCard } from '../../../shared/components/review-card/review-card';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../shared/components/delete-dialog/delete-dialog';

@Component({
    selector: 'app-my-reviews',
    standalone: true,
    imports: [MatIcon, ReviewCard, PaginationComponent, DeleteDialog],
    templateUrl: './my-reviews.html',
})
export class MyReviews {
    private authStore = inject(AuthStore);
    private touristService = inject(TouristService);
    private reviewService = inject(ReviewService);

    userUuid = computed(() => this.authStore.user()?.uuid || '');

    pagination = signal<Pagination<Review> | null>(null);
    reviews = computed<Review[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);
    currentPage = signal(0);

    showDeleteDialog = signal(false);
    reviewToDelete = signal<Review | null>(null);
    isDeleting = signal(false);

    constructor() {
        effect(() => {
            this.loadReviews();
        });
    }

    async loadReviews(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.touristService.getReviews(this.userUuid(), {
                    page: this.currentPage(),
                    size: 10,
                    sortBy: 'createdAt',
                    order: 'desc',
                }),
            );
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load reviews');
        } finally {
            this.isLoading.set(false);
        }
    }

    goToPage(page: number): void {
        this.currentPage.set(page);
        this.loadReviews();
    }

    openDeleteDialog(review: Review): void {
        this.reviewToDelete.set(review);
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        const review = this.reviewToDelete();
        if (!review) return;
        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.reviewService.delete(review.uuid));
            toast.success('Review deleted');
            this.showDeleteDialog.set(false);
            this.reviewToDelete.set(null);
            await this.loadReviews();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete');
        } finally {
            this.isDeleting.set(false);
        }
    }

    truncate(text: string, max: number): string {
        return text?.length > max ? text.substring(0, max) + '...' : text || '';
    }
}
