import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ReviewCard } from '../../../../shared/components/review-card/review-card';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { GuideService } from '../../../../core/services/guide-service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { GuideFind } from '../../../../core/models/guide.model';
import { Review } from '../../../../core/models/review.model';
import { Pagination } from '../../../../core/models/response.models';
import { RoleEnum } from '../../../../core/enums/role.enum';

@Component({
    selector: 'app-guide-detail-public',
    imports: [RouterLink, DatePipe, DecimalPipe, MatIcon, ReviewCard, PaginationComponent],
    templateUrl: './guide-detail-public.html',
})
export class GuideDetailPublic implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private guideService = inject(GuideService);
    private authStore = inject(AuthStore);

    guide = signal<GuideFind | null>(null);
    isLoading = signal(true);

    tours = signal<any[]>([]);
    toursPagination = signal<Pagination<any> | null>(null);
    toursLoading = signal(false);
    toursPage = signal(0);

    reviews = signal<Review[]>([]);
    reviewsPagination = signal<Pagination<Review> | null>(null);
    reviewsLoading = signal(false);
    reviewsPage = signal(0);

    isAuthenticated = computed(() => this.authStore.isAuthenticated());
    isAdmin = computed(() => this.authStore.userRole() === RoleEnum.ADMIN);
    isTourist = computed(() => this.authStore.userRole() === RoleEnum.TOURIST);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/guides']);
            return;
        }
        await this.loadGuide(uuid);
    }

    private async loadGuide(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.guideService.findOne(uuid));
            this.guide.set(res.data as GuideFind);
            await Promise.all([this.loadTours(), this.loadReviews()]);
        } catch {
            toast.error('Guide not found');
            this.router.navigate(['/guides']);
        } finally {
            this.isLoading.set(false);
        }
    }

    async loadTours(): Promise<void> {
        const uuid = this.guide()?.uuid;
        if (!uuid) return;
        this.toursLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.guideService.getTours(uuid, {
                    page: this.toursPage(),
                    size: 4,
                    sortBy: 'rating',
                    order: 'desc',
                }),
            );
            if (res.data) {
                this.toursPagination.set(res.data);
                this.tours.set(res.data.content || []);
            }
        } catch {
        } finally {
            this.toursLoading.set(false);
        }
    }

    goToToursPage(page: number): void {
        this.toursPage.set(page);
        this.loadTours();
    }

    async loadReviews(): Promise<void> {
        const uuid = this.guide()?.uuid;
        if (!uuid) return;
        this.reviewsLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.guideService.getReviews(uuid, {
                    page: this.reviewsPage(),
                    size: 5,
                    sortBy: 'createdAt',
                    order: 'desc',
                }),
            );
            if (res.data) {
                this.reviewsPagination.set(res.data);
                this.reviews.set((res.data.content as Review[]) || []);
            }
        } catch {
        } finally {
            this.reviewsLoading.set(false);
        }
    }

    goToReviewsPage(page: number): void {
        this.reviewsPage.set(page);
        this.loadReviews();
    }

    getInitials(): string {
        const g = this.guide();
        if (!g) return '';
        return `${g.firstName?.charAt(0) || ''}${g.lastName?.charAt(0) || ''}`.toUpperCase();
    }

    getLanguageNames(): string {
        return (
            this.guide()
                ?.languages?.map((l) => l.name)
                .join(', ') || ''
        );
    }

    copyLink(): void {
        navigator.clipboard?.writeText(window.location.href);
        toast.success('Link copied!');
    }

    getStatusColor(status: string): string {
        switch (status) {
            case 'ACTIVE':
                return 'bg-green-500/10 text-green-600';
            case 'INACTIVE':
                return 'bg-gray-500/10 text-gray-500';
            case 'SUSPENDED':
                return 'bg-red-500/10 text-red-600';
            default:
                return 'bg-gray-500/10 text-gray-500';
        }
    }
}
