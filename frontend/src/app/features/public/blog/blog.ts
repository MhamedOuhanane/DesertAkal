import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { ArticleService } from '../../../core/services/article-service';
import { Article } from '../../../core/models/article.models';
import { Pagination } from '../../../core/models/response.models';
import { ArticleDetailDialog } from './article-detail-dialog/article-detail-dialog';

@Component({
    selector: 'app-blog',
    standalone: true,
    imports: [DatePipe, MatIcon, PaginationComponent],
    templateUrl: './blog.html',
})
export class Blog {
    private articleService = inject(ArticleService);
    private dialog = inject(MatDialog);

    pagination = signal<Pagination<Article> | null>(null);
    articles = signal<Article[]>([]);
    isLoading = signal(true);
    currentPage = signal(0);

    async ngOnInit(): Promise<void> {
        await this.loadArticles();
    }

    async loadArticles(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.articleService.findAll({
                    page: this.currentPage(),
                    size: 10,
                    sortBy: 'createdAt',
                    order: 'desc',
                }),
            );
            if (res.data) {
                this.pagination.set(res.data);
                this.articles.set((res.data.content as Article[]) || []);
            }
        } catch {
        } finally {
            this.isLoading.set(false);
        }
    }

    goToPage(page: number): void {
        this.currentPage.set(page);
        this.loadArticles();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    openArticle(article: Article): void {
        this.dialog.open(ArticleDetailDialog, {
            data: { article },
            panelClass: 'transparent-dialog',
            maxWidth: '100vw',
            maxHeight: '100vh',
            width: '100%',
            height: '100%',
            hasBackdrop: false,
        });
    }

    truncate(text: string, max: number): string {
        return text?.length > max ? text.substring(0, max) + '...' : text || '';
    }
}
