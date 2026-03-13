import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { City, CityFilters } from '../../../../core/models/city.model';
import { Pagination } from '../../../../core/models/response.models';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { CityService } from '../../../../core/services/city-service';

@Component({
    selector: 'app-city-list',
    imports: [FormsModule, DecimalPipe, MatIcon, PaginationComponent, DeleteDialog, RouterLink],
    templateUrl: './city-list.html',
})
export class CityList {
    private cityService = inject(CityService);
    private router = inject(Router);

    pagination = signal<Pagination<City> | null>(null);
    cities = computed<City[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<CityFilters>({
        page: 0,
        size: 9,
        sortBy: 'name',
        order: 'asc',
        search: '',
    });

    showDeleteDialog = signal(false);
    cityToDelete = signal<City | null>(null);
    isDeleting = signal(false);

    private searchSubject = new Subject<string>();

    constructor() {
        this.searchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((search) => {
                this.patchQuery({ search, page: 0 });
            });

        effect(() => {
            this.loadCities();
        });
    }

    private patchQuery(patch: Partial<CityFilters>): void {
        this.query.update((prev) => ({ ...prev, ...patch }));
    }

    async loadCities(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(this.cityService.findAll(this.query()));
            if (res.data) {
                this.pagination.set(res.data);
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load cities');
        } finally {
            this.isLoading.set(false);
        }
    }

    onSearch(value: string): void {
        this.searchSubject.next(value);
    }

    onSort(field: string): void {
        const { sortBy, order } = this.query();
        const newOrder = sortBy === field && order === 'asc' ? 'desc' : 'asc';
        this.patchQuery({ sortBy: field, order: newOrder });
        this.loadCities();
    }

    goToPage(page: number): void {
        this.patchQuery({ page });
        this.loadCities();
    }

    viewCity(uuid: string): void {
        this.router.navigate(['/dashboard/cities', uuid]);
    }

    editCity(uuid: string, event: Event): void {
        event.stopPropagation();
        this.router.navigate(['/dashboard/cities', uuid, 'edit']);
    }

    openDeleteDialog(city: City, event: Event): void {
        event.stopPropagation();
        this.cityToDelete.set(city);
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        const city = this.cityToDelete();
        if (!city) return;

        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.cityService.delete(city.uuid));
            toast.success(`City "${city.name}" deleted successfully`);
            this.showDeleteDialog.set(false);
            this.cityToDelete.set(null);
            await this.loadCities();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete city');
            this.showDeleteDialog.set(false);
            this.cityToDelete.set(null);
        } finally {
            this.isDeleting.set(false);
        }
    }

    cancelDelete(): void {
        this.showDeleteDialog.set(false);
        this.cityToDelete.set(null);
    }
}
