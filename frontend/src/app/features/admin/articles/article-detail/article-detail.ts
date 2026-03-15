import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { Comment } from '../../../../core/models/comment.model';
import { Reaction } from '../../../../core/models/reaction.model';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { ArticleService } from '../../../../core/services/article-service';
import { CommentService } from '../../../../core/services/comment-service';
import { Article } from '../../../../core/models/article.models';

@Component({
    selector: 'app-article-detail',
    standalone: true,
    imports: [RouterLink, DatePipe, MatIcon, DeleteDialog],
    templateUrl: './article-detail.html',
})
export class ArticleDetail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private articleService = inject(ArticleService);
    private commentService = inject(CommentService);

    article = signal<Article | null>(null);
    isLoading = signal(true);
    activeTab = signal<'comments' | 'reactions'>('comments');

    comments = signal<Comment[]>([]);
    reactions = signal<Reaction[]>([]);
    commentsLoading = signal(false);
    reactionsLoading = signal(false);
    commentsTotalElements = signal(0);
    reactionsTotalElements = signal(0);

    showDeleteArticleDialog = signal(false);
    isDeletingArticle = signal(false);

    showDeleteCommentDialog = signal(false);
    commentToDelete = signal<Comment | null>(null);
    isDeletingComment = signal(false);

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/dashboard/articles']);
            return;
        }

        await this.loadArticleFromList(uuid);
    }

    private async loadArticleFromList(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.articleService.find(uuid));

            await this.loadComments(uuid);
            await this.loadReactions(uuid);

            if (res) {
                this.article.set(res.data);
            } else {
                toast.error('Article not found');
                this.router.navigate(['/dashboard/articles']);
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Article not found');
            this.router.navigate(['/dashboard/articles']);
        } finally {
            this.isLoading.set(false);
        }
    }

    async loadComments(uuid?: string): Promise<void> {
        const articleUuid = uuid || this.article()?.uuid;
        if (!articleUuid) return;

        this.commentsLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.articleService.getComments(articleUuid, {
                    page: 0,
                    size: 500,
                }),
            );
            this.comments.set((res.data?.content as Comment[]) || []);
            this.commentsTotalElements.set(res.data?.totalElements || 0);
        } catch {
        } finally {
            this.commentsLoading.set(false);
        }
    }

    async loadReactions(uuid?: string): Promise<void> {
        const articleUuid = uuid || this.article()?.uuid;
        if (!articleUuid) return;

        this.reactionsLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.articleService.getReactions(articleUuid, {
                    page: 0,
                    size: 500,
                }),
            );
            this.reactions.set((res.data?.content as Reaction[]) || []);
            this.reactionsTotalElements.set(res.data?.totalElements || 0);
        } catch {
        } finally {
            this.reactionsLoading.set(false);
        }
    }

    switchTab(tab: 'comments' | 'reactions'): void {
        this.activeTab.set(tab);
    }

    openDeleteArticleDialog(): void {
        this.showDeleteArticleDialog.set(true);
    }

    async confirmDeleteArticle(): Promise<void> {
        if (!this.article()) return;
        this.isDeletingArticle.set(true);
        try {
            await firstValueFrom(this.articleService.delete(this.article()!.uuid));
            toast.success('Article deleted successfully');
            this.router.navigate(['/dashboard/articles']);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete article');
        } finally {
            this.isDeletingArticle.set(false);
            this.showDeleteArticleDialog.set(false);
        }
    }

    openDeleteCommentDialog(comment: Comment): void {
        this.commentToDelete.set(comment);
        this.showDeleteCommentDialog.set(true);
    }

    async confirmDeleteComment(): Promise<void> {
        const comment = this.commentToDelete();
        if (!comment) return;

        this.isDeletingComment.set(true);
        try {
            await firstValueFrom(this.commentService.delete(comment.uuid));
            toast.success('Comment deleted');
            this.showDeleteCommentDialog.set(false);
            this.commentToDelete.set(null);
            await this.loadComments();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete comment');
        } finally {
            this.isDeletingComment.set(false);
        }
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
}
