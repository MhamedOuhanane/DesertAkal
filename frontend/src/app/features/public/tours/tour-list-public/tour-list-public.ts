import { Component, computed, effect, inject, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { TourService } from '../../../../core/services/tour-service';
import { CityService } from '../../../../core/services/city-service';
import { Tour, TourFilters } from '../../../../core/models/tour.model';
import { Pagination } from '../../../../core/models/response.models';
import { City } from '../../../../core/models/city.model';
import { CityTour } from '../../../../core/models/city-tour.model';

@Component({
    selector: 'app-tour-list-public',
    imports: [RouterLink, DecimalPipe, MatIcon, PaginationComponent],
    templateUrl: './tour-list-public.html',
})
export class TourListPublic {
    private tourService = inject(TourService);
    private cityService = inject(CityService);
    private platformId = inject(PLATFORM_ID);

    pagination = signal<Pagination<Tour> | null>(null);
    tours = computed<Tour[]>(() => this.pagination()?.content || []);
    cities = signal<City[]>([]);
    isLoading = signal(true);

    query = signal<TourFilters>({
        page: 0,
        size: 9,
        sortBy: 'rating',
        order: 'desc',
        search: '',
        city: '',
        durationStr: '',
        minRating: undefined,
    });

    private searchSubject = new Subject<string>();

    readonly durationOptions = [
        { value: '', label: 'Any Duration' },
        { value: '1-3', label: '1 – 3 days' },
        { value: '4-7', label: '4 – 7 days' },
        { value: '8-14', label: '8 – 14 days' },
        { value: '15-99', label: '15+ days' },
    ];

    readonly ratingOptions = [
        { value: '', label: 'Any Rating' },
        { value: '4', label: '4+ Stars' },
        { value: '3', label: '3+ Stars' },
    ];

    readonly sortOptions = [
        { value: 'rating,desc', label: 'Top Rated' },
        { value: 'createdAt,desc', label: 'Newest' },
        { value: 'durationDays,asc', label: 'Shortest' },
        { value: 'durationDays,desc', label: 'Longest' },
        { value: 'title,asc', label: 'A – Z' },
    ];

    constructor() {
        this.searchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((search) => {
                this.patchQuery({ search, page: 0 });
            });

        effect(() => {
            if (isPlatformBrowser(this.platformId)) {
                this.loadTours();
            } else {
                this.isLoading.set(false);
            }
        });

        if (isPlatformBrowser(this.platformId)) {
            this.loadCities();
        }
    }

    private patchQuery(patch: Partial<TourFilters>): void {
        this.query.update((prev) => ({ ...prev, ...patch }));
    }

    private async loadCities(): Promise<void> {
        try {
            const res = await firstValueFrom(this.cityService.findAll({ page: 0, size: 50 }));
            this.cities.set((res.data?.content as City[]) || []);
        } catch {}
    }

    async loadTours(): Promise<void> {
        this.isLoading.set(true);
        try {
            const q = this.query();
            const params: TourFilters = {
                page: q.page,
                size: q.size,
                sortBy: q.sortBy,
                order: q.order,
            };
            if (q.search) params.search = q.search;
            if (q.city) params.city = q.city;
            if (q.durationStr) params.durationStr = q.durationStr;
            if (q.minRating) params.minRating = q.minRating;

            const res = await firstValueFrom(this.tourService.findAll(params));
            if (res.data) {
                this.pagination.set(res.data);
            }
        } catch {
        } finally {
            this.isLoading.set(false);
        }
    }

    onSearch(value: string): void {
        this.searchSubject.next(value);
    }

    onCityFilter(city: string): void {
        this.patchQuery({ city, page: 0 });
    }

    onDurationFilter(durationStr: string): void {
        this.patchQuery({ durationStr, page: 0 });
    }

    onRatingFilter(value: string): void {
        this.patchQuery({ minRating: value ? Number(value) : undefined, page: 0 });
    }

    onSortChange(value: string): void {
        const [sortBy, order] = value.split(',');
        this.patchQuery({ sortBy, order, page: 0 });
    }

    clearFilters(): void {
        this.query.set({
            page: 0,
            size: 9,
            sortBy: 'rating',
            order: 'desc',
            search: '',
            city: '',
            durationStr: '',
            minRating: undefined,
        });
    }

    goToPage(page: number): void {
        this.patchQuery({ page });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    get hasActiveFilters(): boolean {
        const q = this.query();
        return !!(q.search || q.city || q.durationStr || q.minRating);
    }

    getCityNames(tour: Tour): string {
        if (!tour.cityTours?.length) return '';
        return tour.cityTours
            .map((ct: CityTour) => ct.cityName || '')
            .filter(Boolean)
            .slice(0, 3)
            .join(' · ');
    }
}
