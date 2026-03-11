import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';
import { DecimalPipe } from '@angular/common';
import { AdminService } from '../../../core/services/admin-service';
import { StatCard } from './components/stat-card/stat-card';
import { ActivityItem, RecentActivity } from './components/recent-activity/recent-activity';
import { MonthlyBarChart } from './components/monthly-bar-chart/monthly-bar-chart';
import { StatusDonutChart } from './components/status-donut-chart/status-donut-chart';
import { AdminDashboard, MonthlyStats } from '../../../core/models/admin-dashboard.model';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [
        DecimalPipe,
        StatCard,
        StatusDonutChart,
        MonthlyBarChart,
        RecentActivity,
        DecimalPipe,
        StatCard,
    ],
    templateUrl: './dashboard.html',
})
export class Dashboard implements OnInit {
    private dashboardService = inject(AdminService);

    data = signal<AdminDashboard | null>(null);
    isLoading = signal(true);
    error = signal<string | null>(null);

    async ngOnInit(): Promise<void> {
        await this.loadStats();
    }

    async loadStats(): Promise<void> {
        this.isLoading.set(true);
        this.error.set(null);

        try {
            const response = await firstValueFrom(this.dashboardService.getDashboardStats());
            this.data.set(response.data);
        } catch (err: any) {
            const msg = err?.error?.message || 'Failed to load dashboard statistics.';
            this.error.set(msg);
            toast.error(msg);
        } finally {
            this.isLoading.set(false);
        }
    }

    roundedRating = computed(() => {
        const rating = this.data()?.averageTourRating;
        return rating ? Math.round(rating) : 0;
    });

    get formattedRevenue(): string {
        const r = this.data()?.totalRevenue ?? 0;
        if (r >= 1_000_000) return `€${(r / 1_000_000).toFixed(1)}M`;
        if (r >= 1_000) return `€${(r / 1_000).toFixed(1)}K`;
        return `€${r.toFixed(0)}`;
    }

    get formattedRating(): string {
        return (this.data()?.averageTourRating ?? 0).toFixed(1);
    }

    get activityItems(): ActivityItem[] {
        const d = this.data();
        if (!d) return [];

        return [
            {
                icon: '🧑‍🏫',
                label: 'Active Guides',
                value: d.activeGuides.toString(),
                color: 'bg-primary/10',
            },
            {
                icon: '📝',
                label: 'Published Articles',
                value: d.totalArticles.toString(),
                color: 'bg-info/10',
            },
            {
                icon: '⭐',
                label: 'Average Rating',
                value: `${this.formattedRating}/5`,
                color: 'bg-warning/10',
            },
            {
                icon: '✅',
                label: 'Confirmed Bookings',
                value: (d.reservationsByStatus['CONFIRMED'] ?? 0).toString(),
                color: 'bg-success/10',
            },
            {
                icon: '⏳',
                label: 'Pending Bookings',
                value: (d.reservationsByStatus['PENDING'] ?? 0).toString(),
                color: 'bg-warning/10',
            },
        ];
    }

    get reservationsByStatus(): Record<string, number> {
        return this.data()?.reservationsByStatus ?? {};
    }

    get monthlyPerformance(): MonthlyStats[] {
        return this.data()?.monthlyPerformance ?? [];
    }
}
