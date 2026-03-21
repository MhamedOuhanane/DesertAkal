import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ReviewCard } from '../../../../shared/components/review-card/review-card';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { IsAuthenticated } from '../../../../shared/directives';
import { TourService } from '../../../../core/services/tour-service';
import { ReviewService } from '../../../../core/services/review-service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { TourFind } from '../../../../core/models/tour.model';
import { Review } from '../../../../core/models/review.model';
import { Pagination } from '../../../../core/models/response.models';
import { RoleEnum } from '../../../../core/enums/role.enum';
import { CityTourFind } from '../../../../core/models/city-tour.model';

@Component({
    selector: 'app-tour-detail-public',
    imports: [RouterLink, DecimalPipe, MatIcon, ReviewCard, PaginationComponent, IsAuthenticated],
    templateUrl: './tour-detail-public.html',
})
export class TourDetailPublic implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private tourService = inject(TourService);
    private authStore = inject(AuthStore);

    tour = signal<TourFind | null>(null);
    isLoading = signal(true);
    reviews = signal<Review[]>([]);
    reviewPagination = signal<Pagination<Review> | null>(null);
    reviewsLoading = signal(false);
    reviewPage = signal(0);
    isAuthenticated = computed(() => this.authStore.isAuthenticated());
    isTourist = computed(() => this.authStore.userRole() === RoleEnum.TOURIST);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/tours']);
            return;
        }
        await this.loadTour(uuid);
        await this.loadReviews(uuid);
    }

    private async loadTour(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.tourService.findOne(uuid));
            this.tour.set(res.data as TourFind);
        } catch {
            toast.error('Tour not found');
            this.router.navigate(['/tours']);
        } finally {
            this.isLoading.set(false);
        }
    }

    async loadReviews(tourUuid?: string): Promise<void> {
        const uuid = tourUuid || this.tour()?.uuid;
        if (!uuid) return;
        this.reviewsLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.tourService.getReviews(uuid, {
                    page: this.reviewPage(),
                    size: 5,
                    sortBy: 'createdAt',
                    order: 'desc',
                }),
            );
            if (res.data) {
                this.reviewPagination.set(res.data);
                this.reviews.set((res.data.content as Review[]) || []);
            }
        } catch {
        } finally {
            this.reviewsLoading.set(false);
        }
    }

    goToReviewPage(page: number): void {
        this.reviewPage.set(page);
        this.loadReviews();
    }

    bookNow(): void {
        if (!this.isAuthenticated()) {
            this.router.navigate(['/auth/login'], { queryParams: { redirect: this.router.url } });
            return;
        }
        if (this.isTourist()) {
            this.router.navigate(['/tourist/dashboard/bookings/new'], {
                queryParams: { tourUuid: this.tour()?.uuid },
            });
        } else {
            toast.info('Only tourists can make bookings');
        }
    }

    getCityRoute(): string {
        return (
            this.tour()
                ?.cityTours?.map((c) => c.city?.name || '')
                .filter(Boolean)
                .join(' → ') || ''
        );
    }

    getSortedCityTours(): (CityTourFind & { startDay: number; endDay: number })[] {
        const sorted = [...(this.tour()?.cityTours || [])].sort(
            (a, b) => (a.orderIndex || 0) - (b.orderIndex || 0),
        );

        let currentDay = 1;
        return sorted.map((ct) => {
            const startDay = currentDay;
            const endDay = currentDay + (ct.daysCount || 1) - 1;
            currentDay = endDay + 1;
            return { ...ct, startDay, endDay };
        });
    }

    copyLink(): void {
        navigator.clipboard?.writeText(window.location.href);
        toast.success('Link copied!');
    }
}
