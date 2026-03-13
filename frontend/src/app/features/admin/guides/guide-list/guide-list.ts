import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { GuideService } from '../../../../core/services/guide-service';
import { Guide, GuideFilters } from '../../../../core/models/guide.model';
import { Pagination } from '../../../../core/models/response.models';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';

@Component({
    selector: 'app-guide-list',
    imports: [RouterLink, FormsModule, DecimalPipe, PaginationComponent],
    templateUrl: './guide-list.html',
})
export class GuideList {
    private guideService = inject(GuideService);
    private router = inject(Router);

    pagination = signal<Pagination<Guide> | null>(null);
    guide = computed(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<GuideFilters>({
        page: 0,
        size: 10,
        sortBy: 'lastLoginAt',
        order: 'desc',
        search: '',
        language: '',
    });

    private searchSubject = new Subject<string>();

    constructor() {
        this.searchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((value) => {
                this.patchQuery({ search: value, page: 0 });
            });

        effect(
            () => {
                this.loadGuides();
            },
            { allowSignalWrites: true },
        );
    }

    private patchQuery(patch: Partial<GuideFilters>) {
        this.query.update((prev) => ({ ...prev, ...patch }));
    }

    async loadGuides(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(this.guideService.findAll(this.query()));
            if (res.data) {
                this.pagination.set(res.data);
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load guides');
        } finally {
            this.isLoading.set(false);
        }
    }

    onPageChange(page: number): void {
        this.patchQuery({ page: page });
        this.loadGuides();
    }

    onSearch(value: string): void {
        this.searchSubject.next(value);
    }

    onLanguageFilter(value: string): void {
        this.patchQuery({ language: value, page: 0 });
    }

    onSort(field: string): void {
        const { sortBy, order } = this.query();
        const newOrder = sortBy === field && order === 'asc' ? 'desc' : 'asc';
        this.patchQuery({ sortBy: field, order: newOrder });
    }

    viewGuide(uuid: string): void {
        this.router.navigate(['/dashboard/guides', uuid]);
    }

    editGuide(uuid: string): void {
        this.router.navigate(['/dashboard/guides', uuid, 'edit']);
    }

    getInitials(firstName: string, lastName: string): string {
        return `${firstName?.charAt(0) || ''}${lastName?.charAt(0) || ''}`.toUpperCase();
    }

    getRatingColor(rating: number): string {
        if (rating >= 4.5) return 'text-green-600';
        if (rating >= 3.5) return 'text-primary';
        if (rating >= 2.5) return 'text-warning';
        return 'text-red-500';
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
