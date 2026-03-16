import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { Review, ReviewFilters } from '../../../../core/models/review.model';
import { Pagination } from '../../../../core/models/response.models';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { ReviewService } from '../../../../core/services/review-service';
import { ReviewableType } from '../../../../core/enums/reviewable-type.enum';

@Component({
    selector: 'app-review-list',
    imports: [
        FormsModule,
        DatePipe,
        DecimalPipe,
        RouterLink,
        MatIcon,
        PaginationComponent,
        DeleteDialog,
    ],
    templateUrl: './review-list.html',
})
export class ReviewList {
    private reviewService = inject(ReviewService);

    pagination = signal<Pagination<Review> | null>(null);
    reviews = computed<Review[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<ReviewFilters>({
        page: 0,
        size: 6,
        sortBy: 'createdAt',
        order: 'desc',
        type: undefined,
        minRating: 0,
    });

    showDeleteDialog = signal(false);
    reviewToDelete = signal<Review | null>(null);
    isDeleting = signal(false);

    expandedReview = signal<string | null>(null);

    constructor() {
        effect(() => {
            this.loadReviews();
        });
    }

    async loadReviews(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(this.reviewService.findAll(this.query()));
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load reviews');
        } finally {
            this.isLoading.set(false);
        }
    }

    onTypeFilter(value: string): void {
        const type = value ? (value as ReviewableType) : undefined;
        this.query.update((q) => ({ ...q, type, page: 0 }));
        this.loadReviews();
    }

    onMinRatingFilter(value: string): void {
        const minRating = value ? Number(value) : undefined;
        this.query.update((q) => ({ ...q, minRating, page: 0 }));
        this.loadReviews();
    }

    onSort(field: string): void {
        const { sortBy, order } = this.query();
        const newOrder = sortBy === field && order === 'asc' ? 'desc' : 'asc';
        this.query.update((q) => ({ ...q, sortBy: field, order: newOrder }));
        this.loadReviews();
    }

    goToPage(page: number): void {
        this.query.update((q) => ({ ...q, page }));
        this.loadReviews();
    }

    toggleExpand(uuid: string): void {
        this.expandedReview.update((current) => (current === uuid ? null : uuid));
    }

    openDeleteDialog(review: Review, event: Event): void {
        event.stopPropagation();
        this.reviewToDelete.set(review);
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        const review = this.reviewToDelete();
        if (!review) return;

        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.reviewService.delete(review.uuid));
            toast.success('Review deleted successfully');
            this.showDeleteDialog.set(false);
            this.reviewToDelete.set(null);
            await this.loadReviews();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete review');
        } finally {
            this.isDeleting.set(false);
        }
    }

    cancelDelete(): void {
        this.showDeleteDialog.set(false);
        this.reviewToDelete.set(null);
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

    getStarArray(rating: number): string[] {
        return Array.from({ length: 5 }, (_, i) => {
            const starValue = i + 1;

            if (rating >= starValue) return 'full';
            else if (rating >= starValue - 0.5) return 'half';
            else return 'empty';
        });
    }

    getRatingColor(rating: number): string {
        if (rating >= 4) return 'text-green-600';
        if (rating >= 3) return 'text-primary';
        if (rating >= 2) return 'text-warning';
        return 'text-red-500';
    }

    getRatingBg(rating: number): string {
        if (rating >= 4) return 'bg-green-500/10';
        if (rating >= 3) return 'bg-primary/10';
        if (rating >= 2) return 'bg-warning/10';
        return 'bg-red-500/10';
    }

    getTypeBadge(type: string): { bg: string; icon: string } {
        if (type === 'TOUR') {
            return { bg: 'bg-blue-500/10 text-blue-600', icon: 'map' };
        }
        return { bg: 'bg-purple-500/10 text-purple-600', icon: 'person' };
    }

    truncate(text: string, max: number): string {
        return text?.length > max ? text.substring(0, max) + '...' : text || '';
    }

    readonly types = [
        { value: '', label: 'All Types' },
        { value: 'TOUR', label: 'Tour Reviews' },
        { value: 'GUIDE', label: 'Guide Reviews' },
    ];

    readonly ratingOptions = [
        { value: '', label: 'All Ratings' },
        { value: '4', label: '★ 4+' },
        { value: '3', label: '★ 3+' },
        { value: '2', label: '★ 2+' },
        { value: '1', label: '★ 1+' },
    ];
}
