import { Component, computed, effect, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { AuthStore } from '../../../core/auth/auth.store';
import { GuideService } from '../../../core/services/guide-service';
import { Pagination } from '../../../core/models/response.models';
import { Tour } from '../../../core/models/tour.model';

@Component({
    selector: 'app-guide-my-tours',
    imports: [MatIcon, PaginationComponent],
    templateUrl: './my-tours.html',
})
export class MyTours {
    private authStore = inject(AuthStore);
    private guideService = inject(GuideService);

    userUuid = computed(() => this.authStore.user()?.uuid || '');

    pagination = signal<Pagination<Tour> | null>(null);
    tours = computed<Tour[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);
    currentPage = signal(0);

    constructor() {
        effect(() => {
            this.loadTours();
        });
    }

    async loadTours(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.guideService.getTours(this.userUuid(), {
                    page: this.currentPage(),
                    size: 9,
                }),
            );
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load tours');
        } finally {
            this.isLoading.set(false);
        }
    }

    goToPage(page: number): void {
        this.currentPage.set(page);
        this.loadTours();
    }
}
