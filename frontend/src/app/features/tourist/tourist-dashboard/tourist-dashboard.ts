import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { Router } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { UserService } from '../../../core/services/user-service';
import { MatIcon } from '@angular/material/icon';
import { StatsCard } from '../../../shared/components/stats-card/stats-card';
import { ReservationCard } from '../../../shared/components/reservation-card/reservation-card';
import { Reservation } from '../../../core/models/reservation.model';
import { Article } from '../../../core/models/article.models';
import { TouristService } from '../../../core/services/tourist-service';

@Component({
    selector: 'app-tourist-dashboard',
    standalone: true,
    imports: [RouterLink, MatIcon, StatsCard, ReservationCard],
    templateUrl: './tourist-dashboard.html',
})
export class TouristDashboard implements OnInit {
    private authStore = inject(AuthStore);
    private touristService = inject(TouristService);
    private userService = inject(UserService);
    private router = inject(Router);

    isLoading = signal(true);
    userName = computed(() => this.authStore.user()?.fullName || 'Tourist');
    userUuid = computed(() => this.authStore.user()?.uuid || '');

    bookingsCount = signal(0);
    pendingCount = signal(0);
    articlesCount = signal(0);
    reviewsCount = signal(0);

    recentBookings = signal<Reservation[]>([]);
    recentArticles = signal<Article[]>([]);

    async ngOnInit(): Promise<void> {
        await Promise.all([
            this.loadBookingsStats(),
            this.loadRecentBookings(),
            this.loadArticlesStats(),
            this.loadReviewsStats(),
        ]);
        this.isLoading.set(false);
    }

    private async loadBookingsStats(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.touristService.getReservations(this.userUuid(), {
                    page: 0,
                    size: 1,
                }),
            );
            this.bookingsCount.set(res.data?.totalElements || 0);

            const pending = await firstValueFrom(
                this.touristService.getReservations(this.userUuid(), {
                    page: 0,
                    size: 1,
                    status: 'PENDING',
                }),
            );
            this.pendingCount.set(pending.data?.totalElements || 0);
        } catch {}
    }

    private async loadRecentBookings(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.touristService.getReservations(this.userUuid(), {
                    page: 0,
                    size: 3,
                    sortBy: 'createdAt',
                    order: 'desc',
                }),
            );
            this.recentBookings.set((res.data?.content as Reservation[]) || []);
        } catch {}
    }

    private async loadArticlesStats(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.userService.getArticles(this.userUuid(), {
                    page: 0,
                    size: 1,
                }),
            );
            this.articlesCount.set(res.data?.totalElements || 0);
        } catch {}
    }

    private async loadReviewsStats(): Promise<void> {
        try {
            const res = await firstValueFrom(
                this.touristService.getReviews(this.userUuid(), {
                    page: 0,
                    size: 1,
                }),
            );
            this.reviewsCount.set(res.data?.totalElements || 0);
        } catch {}
    }

    viewBooking(uuid: string): void {
        this.router.navigate(['/tourist/dashboard/bookings', uuid]);
    }

    payBooking(uuid: string): void {
        this.router.navigate(['/tourist/dashboard/bookings', uuid, 'pay']);
    }

    getGreeting(): string {
        const hour = new Date().getHours();
        if (hour < 12) return 'Good morning';
        if (hour < 18) return 'Good afternoon';
        return 'Good evening';
    }
}
