import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Pagination } from '../../../core/models/response.models';

@Component({
    selector: 'app-pagination',
    imports: [CommonModule],
    templateUrl: './pagination.html',
})
export class PaginationComponent<T> {
    pagination = input.required<Pagination<T>>();
    pageChange = output<number>();
    

    get totalPages(): number {
        return this.pagination()?.totalPages ?? 0;
    }

    get pageNumbers(): number[] {
        const total = this.pagination()?.totalPages || 0;
        const current = this.pagination()?.page || 0;
        const pages: number[] = [];
        const start = Math.max(0, current - 2);
        const end = Math.min(total - 1, current + 2);
        for (let i = start; i <= end; i++) pages.push(i);
        return pages;
    }

    goToPage(page: number) {
        if (page >= 0 && page < this.pagination().totalPages) {
            this.pageChange.emit(page);
        }
    }
}
