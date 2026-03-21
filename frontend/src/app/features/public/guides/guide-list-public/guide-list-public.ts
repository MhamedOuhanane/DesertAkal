import { Component, computed, effect, inject, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged, firstValueFrom } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatIcon } from '@angular/material/icon';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { GuideService } from '../../../../core/services/guide-service';
import { Guide } from '../../../../core/models/guide.model';
import { Pagination } from '../../../../core/models/response.models';

interface GuideFilters {
    page: number;
    size: number;
    sortBy: string;
    order: string;
    search: string;
}

@Component({
    selector: 'app-guide-list-public',
    imports: [RouterLink, DecimalPipe, MatIcon, PaginationComponent],
    templateUrl: './guide-list-public.html',
})
export class GuideListPublic {
    private guideService = inject(GuideService);
    private platformId = inject(PLATFORM_ID);

    pagination = signal<Pagination<Guide> | null>(null);
    guides = computed<Guide[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<GuideFilters>({
        page: 0,
        size: 12,
        sortBy: 'rating',
        order: 'desc',
        search: '',
    });

    private searchSubject = new Subject<string>();

    readonly sortOptions = [
        { value: 'rating,desc', label: 'Top Rated' },
        { value: 'firstName,asc', label: 'Name A – Z' },
        { value: 'createdAt,desc', label: 'Newest' },
    ];

    constructor() {
        this.searchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((search) => {
                this.patchQuery({ search, page: 0 });
            });

        effect(() => {
            if (isPlatformBrowser(this.platformId)) {
                this.loadGuides();
            } else {
                this.isLoading.set(false);
            }
        });
    }

    private patchQuery(patch: Partial<GuideFilters>): void {
        this.query.update((prev) => ({ ...prev, ...patch }));
    }

    async loadGuides(): Promise<void> {
        this.isLoading.set(true);
        try {
            const q = this.query();
            const params: any = {
                page: q.page,
                size: q.size,
                sortBy: q.sortBy,
                order: q.order,
            };
            if (q.search) params.search = q.search;

            const res = await firstValueFrom(this.guideService.findAll(params));
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

    onSortChange(value: string): void {
        const [sortBy, order] = value.split(',');
        this.patchQuery({ sortBy, order, page: 0 });
    }

    goToPage(page: number): void {
        this.patchQuery({ page });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    getLanguageNames(guide: Guide): string {
        return (
            guide.languages
                ?.map((l) => l.name)
                .slice(0, 4)
                .join(', ') || ''
        );
    }

    get currentSort(): string {
        const q = this.query();
        return `${q.sortBy},${q.order}`;
    }
}
