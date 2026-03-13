import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { GuideService } from '../../../../core/services/guide-service';
import { GuideFind } from '../../../../core/models/guide.model';

@Component({
    selector: 'app-guide-detail',
    imports: [RouterLink, DatePipe, DecimalPipe],
    templateUrl: './guide-detail.html',
})
export class GuideDetail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private guideService = inject(GuideService);

    guide = signal<GuideFind | null>(null);
    isLoading = signal(true);
    activeTab = signal<'tours' | 'reservations' | 'reviews'>('tours');

    tours = signal<any[]>([]);
    reviews = signal<any[]>([]);
    toursLoading = signal(false);
    reviewsLoading = signal(false);
    toursTotalElements = signal(0);
    reviewsTotalElements = signal(0);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/dashboard/guides']);
            return;
        }
        await this.loadGuide(uuid);
    }

    private async loadGuide(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.guideService.findOne(uuid));
            this.guide.set(res.data);
            await this.loadTours();
            await this.loadReviews();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Guide not found');
            this.router.navigate(['/dashboard/guides']);
        } finally {
            this.isLoading.set(false);
        }
    }

    async loadTours(): Promise<void> {
        if (!this.guide()) return;
        this.toursLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.guideService.getTours(this.guide()!.uuid, {
                    page: 0,
                    size: 5,
                    sortBy: 'createdAt',
                    order: 'desc',
                }),
            );
            this.tours.set(res.data?.content || []);
            this.toursTotalElements.set(res.data?.totalElements || 0);
        } catch {
            // Silent fail - tours section just shows empty
        } finally {
            this.toursLoading.set(false);
        }
    }

    async loadReviews(): Promise<void> {
        if (!this.guide()) return;
        this.reviewsLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.guideService.getReviews(this.guide()!.uuid, {
                    page: 0,
                    size: 5,
                }),
            );
            this.reviews.set(res.data?.content || []);
            this.reviewsTotalElements.set(res.data?.totalElements || 0);
        } catch {
            // Silent fail
        } finally {
            this.reviewsLoading.set(false);
        }
    }

    switchTab(tab: 'tours' | 'reservations' | 'reviews'): void {
        this.activeTab.set(tab);
    }

    getInitials(firstName: string, lastName: string): string {
        return `${firstName?.charAt(0) || ''}${lastName?.charAt(0) || ''}`.toUpperCase();
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

    getRatingStars(rating: number): number[] {
        return Array.from({ length: 5 }, (_, i) => i);
    }
}
