import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { Pagination } from '../../../../core/models/response.models';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { ArticleService } from '../../../../core/services/article-service';
import { Article, ArticleFilters } from '../../../../core/models/article.models';

@Component({
    selector: 'app-article-list',
    standalone: true,
    imports: [FormsModule, DatePipe, MatIcon, PaginationComponent, DeleteDialog],
    templateUrl: './article-list.html',
})
export class ArticleList {
    private articleService = inject(ArticleService);
    private router = inject(Router);

    pagination = signal<Pagination<Article> | null>(null);
    articles = computed<Article[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);

    query = signal<ArticleFilters>({
        page: 0,
        size: 9,
        sortBy: 'createdAt',
        order: 'desc',
        search: '',
    });

    showDeleteDialog = signal(false);
    articleToDelete = signal<Article | null>(null);
    isDeleting = signal(false);

    private searchSubject = new Subject<string>();

    constructor() {
        this.searchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((search) => {
                this.query.update((q) => ({ ...q, search, page: 0 }));
            });

        effect(() => {
            this.loadArticles();
        });
    }

    async loadArticles(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(this.articleService.findAll(this.query()));
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load articles');
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
        this.query.update((q) => ({ ...q, sortBy: field, order: newOrder }));
        this.loadArticles();
    }

    goToPage(page: number): void {
        this.query.update((q) => ({ ...q, page }));
        this.loadArticles();
    }

    viewArticle(uuid: string): void {
        this.router.navigate(['/dashboard/articles', uuid]);
    }

    openDeleteDialog(article: Article, event: Event): void {
        event.stopPropagation();
        this.articleToDelete.set(article);
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        const article = this.articleToDelete();
        if (!article) return;

        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.articleService.delete(article.uuid));
            toast.success('Article deleted successfully');
            this.showDeleteDialog.set(false);
            this.articleToDelete.set(null);
            await this.loadArticles();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete article');
        } finally {
            this.isDeleting.set(false);
        }
    }

    cancelDelete(): void {
        this.showDeleteDialog.set(false);
        this.articleToDelete.set(null);
    }

    getInitials(name: string): string {
        return (
            name
                ?.split(' ')
                .map((w) => w.charAt(0))
                .join('')
                .toUpperCase()
                .slice(0, 2) || '?'
        );
    }

    truncate(text: string, max: number): string {
        return text?.length > max ? text.substring(0, max) + '...' : text;
    }
}
