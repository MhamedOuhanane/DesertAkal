import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { ArticleCard } from '../../../shared/components/article-card/article-card';
import { PaginationComponent } from '../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../shared/components/delete-dialog/delete-dialog';
import { AuthStore } from '../../../core/auth/auth.store';
import { UserService } from '../../../core/services/user-service';
import { ArticleService } from '../../../core/services/article-service';
import { Pagination } from '../../../core/models/response.models';
import { Article } from '../../../core/models/article.models';

@Component({
    selector: 'app-my-posts',
    standalone: true,
    imports: [RouterLink, MatIcon, ArticleCard, PaginationComponent, DeleteDialog],
    templateUrl: './my-posts.html',
})
export class MyPosts {
    private authStore = inject(AuthStore);
    private userService = inject(UserService);
    private articleService = inject(ArticleService);
    private router = inject(Router);

    userUuid = computed(() => this.authStore.user()?.uuid || '');

    pagination = signal<Pagination<Article> | null>(null);
    articles = computed<Article[]>(() => this.pagination()?.content || []);
    isLoading = signal(true);
    currentPage = signal(0);

    showDeleteDialog = signal(false);
    articleToDelete = signal<Article | null>(null);
    isDeleting = signal(false);

    constructor() {
        effect(() => {
            this.loadArticles();
        });
    }

    async loadArticles(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.userService.getArticles(this.userUuid(), {
                    page: this.currentPage(),
                    size: 9,
                }),
            );
            if (res.data) this.pagination.set(res.data);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load posts');
        } finally {
            this.isLoading.set(false);
        }
    }

    goToPage(page: number): void {
        this.currentPage.set(page);
        this.loadArticles();
    }

    viewArticle(uuid: string): void {
        this.router.navigate(['/tourist/dashboard/posts', uuid, 'edit']);
    }

    createPost(): void {
        this.router.navigate(['/tourist/dashboard/posts/create']);
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
            toast.success('Post deleted');
            this.showDeleteDialog.set(false);
            this.articleToDelete.set(null);
            await this.loadArticles();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete');
        } finally {
            this.isDeleting.set(false);
        }
    }

    truncate(text: string, max: number): string {
        return text?.length > max ? text.substring(0, max) + '...' : text || '';
    }
}
