import { Component, inject, OnInit, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { HeroSection } from './components/hero-section/hero-section';
import { PopularTours } from './components/popular-tours/popular-tours';
import { FeaturesSection } from './components/features-section/features-section';
import { TopGuidesSection } from './components/top-guides-section/top-guides-section';
import { ReviewsSection } from './components/reviews-section/reviews-section';
import { CtaSection } from './components/cta-section/cta-section';
import { TourService } from '../../../core/services/tour-service';
import { GuideService } from '../../../core/services/guide-service';
import { Guide } from '../../../core/models/guide.model';
import { Tour } from '../../../core/models/tour.model';
import { HomeReview } from '../../../core/models/review.model';

@Component({
    selector: 'app-home',
    imports: [
        HeroSection,
        PopularTours,
        FeaturesSection,
        TopGuidesSection,
        ReviewsSection,
        CtaSection,
    ],
    templateUrl: './home.html',
})
export class Home implements OnInit {
    private tourService = inject(TourService);
    private guideService = inject(GuideService);
    private platformId = inject(PLATFORM_ID);

    popularTours = signal<Tour[]>([]);
    topGuides = signal<Guide[]>([]);
    latestReviews = signal<HomeReview[]>([]);
    toursLoading = signal(true);
    guidesLoading = signal(true);
    reviewsLoading = signal(true);

    async ngOnInit(): Promise<void> {
        if (!isPlatformBrowser(this.platformId)) {
            this.toursLoading.set(false);
            this.guidesLoading.set(false);
            this.reviewsLoading.set(false);
            return;
        }

        await Promise.all([this.loadTopTours(), this.loadGuides()]);
        await this.loadReviews();
    }

    private async loadTopTours(): Promise<void> {
        try {
            const res = await firstValueFrom(this.tourService.getTop5Tours());
            this.popularTours.set(res.data || []);
        } catch {
        } finally {
            this.toursLoading.set(false);
        }
    }

    private async loadGuides(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.guideService.findAll({
                    page: 0,
                    size: 4,
                    sortBy: 'rating',
                    order: 'desc',
                }),
            );
            this.topGuides.set((res.data?.content as Guide[]) || []);
        } catch {
        } finally {
            this.guidesLoading.set(false);
        }
    }

    private async loadReviews(): Promise<void> {
        try {
            const tours = this.popularTours();
            if (!tours.length) {
                this.reviewsLoading.set(false);
                return;
            }

            const promises = tours.slice(0, 3).map((tour) =>
                firstValueFrom(
                    this.tourService.getReviews(tour.uuid, {
                        page: 0,
                        size: 1,
                        sortBy: 'rating',
                        order: 'desc',
                    }),
                )
                    .then((res) => {
                        const r = res.data?.content?.[0];
                        return r ? this.toHomeReview(r, tour.title) : null;
                    })
                    .catch(() => null),
            );

            const results = (await Promise.all(promises)).filter(
                (r): r is HomeReview => r !== null,
            );
            this.latestReviews.set(results);
        } catch {
        } finally {
            this.reviewsLoading.set(false);
        }
    }

    private toHomeReview(review: any, tourTitle: string): HomeReview {
        const first = review.touristFirstName || review.tourist?.firstName || '';
        const last = review.touristLastName || review.tourist?.lastName || '';
        const name = `${first} ${last}`.trim() || 'Traveler';

        return {
            comment: review.comment || review.text || '',
            rating: review.rating || 0,
            authorName: name,
            authorInitial: name.charAt(0).toUpperCase(),
            tourTitle,
        };
    }
}
