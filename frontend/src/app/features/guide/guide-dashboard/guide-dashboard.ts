import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { Router } from '@angular/router';
import { StatsCard } from '../../../shared/components/stats-card/stats-card';
import { ReservationCard } from '../../../shared/components/reservation-card/reservation-card';
import { AuthStore } from '../../../core/auth/auth.store';
import { GuideService } from '../../../core/services/guide-service';
import { Reservation } from '../../../core/models/reservation.model';
import { Tour } from '../../../core/models/tour.model';

@Component({
    selector: 'app-guide-dashboard',
    standalone: true,
    imports: [RouterLink, MatIcon, StatsCard, ReservationCard],
    templateUrl: './guide-dashboard.html',
})
export class GuideDashboard implements OnInit {
    private authStore = inject(AuthStore);
    private guideService = inject(GuideService);
    private router = inject(Router);

    isLoading = signal(true);
    userName = computed(() => this.authStore.user()?.fullName || 'Guide');
    userUuid = computed(() => this.authStore.user()?.uuid || '');

    toursCount = signal(0);
    totalReservations = signal(0);
    upcomingCount = signal(0);
    reviewsCount = signal(0);
    averageRating = signal(0);

    upcomingAssignments = signal<Reservation[]>([]);
    myTours = signal<Tour[]>([]);

    async ngOnInit(): Promise<void> {
        await Promise.all([
            this.loadToursStats(),
            this.loadReservationsStats(),
            this.loadUpcomingAssignments(),
            this.loadReviewsStats(),
            this.loadMyTours(),
        ]);
        this.isLoading.set(false);
    }

    private async loadToursStats(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.guideService.getTours(this.userUuid(), { page: 0, size: 1 }),
            );
            this.toursCount.set(res.data?.totalElements || 0);
        } catch {}
    }

    private async loadReservationsStats(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.guideService.getReservations(this.userUuid(), { page: 0, size: 1 }),
            );
            this.totalReservations.set(res.data?.totalElements || 0);

            const upcoming = await firstValueFrom(
                this.guideService.getReservations(this.userUuid(), {
                    page: 0,
                    size: 1,
                    status: 'CONFIRMED',
                }),
            );
            this.upcomingCount.set(upcoming.data?.totalElements || 0);
        } catch {}
    }

    private async loadUpcomingAssignments(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.guideService.getReservations(this.userUuid(), {
                    page: 0,
                    size: 4,
                    status: 'CONFIRMED',
                    sortBy: 'startDate',
                    order: 'asc',
                }),
            );
            this.upcomingAssignments.set((res.data?.content as Reservation[]) || []);
        } catch {}
    }

    private async loadReviewsStats(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.guideService.getReviews(this.userUuid(), { page: 0, size: 1 }),
            );
            this.reviewsCount.set(res.data?.totalElements || 0);
        } catch {}
    }

    private async loadMyTours(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.guideService.getTours(this.userUuid(), { page: 0, size: 3 }),
            );
            this.myTours.set((res.data?.content as Tour[]) || []);
        } catch {}
    }

    viewAssignment(uuid: string): void {
        this.router.navigate(['/guide/dashboard/assignments', uuid]);
    }

    getGreeting(): string {
        const hour = new Date().getHours();
        if (hour < 12) return 'Good morning';
        if (hour < 18) return 'Good afternoon';
        return 'Good evening';
    }
}
