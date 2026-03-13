import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { Tour, TourFilters } from '../../../../core/models/tour.model';
import { Pagination } from '../../../../core/models/response.models';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';
import { TourService } from '../../../../core/services/tour-service';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';

@Component({
    selector: 'app-tour-list',
    standalone: true,
    imports: [RouterLink, FormsModule, DecimalPipe, PaginationComponent, DeleteDialog],
    templateUrl: './tour-list.html',
})
export class TourList implements OnInit {
    private tourService = inject(TourService);
    private router = inject(Router);

    tours = signal<Tour[]>([]);
    pagination = signal<Pagination<Tour> | null>(null);
    isLoading = signal(true);
    isDeleting = signal<string | null>(null);

    showDeleteDialog = signal(false);
    tourToDelete = signal<Tour | null>(null);

    search = '';
    sortBy = 'createdAt';
    order = 'desc';
    currentPage = 0;
    pageSize = 10;

    async ngOnInit(): Promise<void> {
        await this.loadTours();
    }

    async loadTours(): Promise<void> {
        this.isLoading.set(true);
        try {
            const filters: TourFilters = {
                search: this.search || undefined,
                page: this.currentPage,
                size: this.pageSize,
                sortBy: this.sortBy,
                order: this.order,
            };
            const res = await firstValueFrom(this.tourService.findAll(filters));
            if (res.data) {
                this.tours.set(res.data.content);
                this.pagination.set(res.data);
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load tours.');
        } finally {
            this.isLoading.set(false);
        }
    }

    onSearch(): void {
        this.currentPage = 0;
        this.loadTours();
    }

    onSortChange(): void {
        this.currentPage = 0;
        this.loadTours();
    }

    viewTour(uuid: string): void {
        this.router.navigate(['/dashboard/tours', uuid]);
    }

    editTour(uuid: string): void {
        this.router.navigate(['/dashboard/tours', uuid, 'edit']);
    }

    confirmDelete(tour: Tour): void {
        this.tourToDelete.set(tour);
        this.showDeleteDialog.set(true);
    }

    cancelDelete(): void {
        this.showDeleteDialog.set(false);
        this.tourToDelete.set(null);
    }

    async deleteTour(): Promise<void> {
        const tour = this.tourToDelete();
        if (!tour) return;

        this.showDeleteDialog.set(false);
        this.isDeleting.set(tour.uuid);

        try {
            await firstValueFrom(this.tourService.delete(tour.uuid));
            this.tours.update((list) => list.filter((t) => t.uuid !== tour.uuid));
            toast.success(`"${tour.title}" deleted successfully.`);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete tour.');
            this.showDeleteDialog.set(false);
        } finally {
            this.isDeleting.set(null);
            this.tourToDelete.set(null);
        }
    }

    onPageChange(page: number): void {
        this.currentPage = page;
        this.loadTours();
    }
}
