import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AuthStore } from '../../../../core/auth/auth.store';
import { Article } from '../../../../core/models/article.models';
import { Comment } from '../../../../core/models/comment.model';
import { ReactionSummary } from '../../../../core/models/reaction.model';
import { IsAuthenticated } from '../../../../shared/directives';
import { ReactionService } from '../../../../core/services/reaction-service';
import { CommentService } from '../../../../core/services/comment-service';
import { ReactionType } from '../../../../core/enums/reaction.enum';
import { ArticleService } from '../../../../core/services/article-service';

@Component({
    selector: 'app-article-detail-dialog',
    imports: [DatePipe, RouterLink, MatIcon, IsAuthenticated],
    templateUrl: './article-detail-dialog.html',
})
export class ArticleDetailDialog implements OnInit {
    private dialogRef = inject(MatDialogRef<ArticleDetailDialog>);
    private data: { article: Article } = inject(MAT_DIALOG_DATA);
    private reactionService = inject(ReactionService);
    private articleService = inject(ArticleService);
    private commentService = inject(CommentService);
    private authStore = inject(AuthStore);

    article = this.data.article;
    isAuthenticated = computed(() => this.authStore.isAuthenticated());

    reactionSummary = signal<ReactionSummary | null>(null);
    reactionsLoading = signal(true);
    userReaction = computed(() => this.reactionSummary()?.userReaction || null);

    comments = signal<Comment[]>([]);
    commentsLoading = signal(true);
    newComment = signal('');
    submitting = signal(false);

    readonly reactionEmojis: { type: ReactionType; emoji: string; label: string }[] = [
        { type: ReactionType.LIKE, emoji: '👍', label: 'Like' },
        { type: ReactionType.HEART, emoji: '❤️', label: 'Love' },
        { type: ReactionType.LAUGH, emoji: '😂', label: 'Haha' },
        { type: ReactionType.WOW, emoji: '😮', label: 'Wow' },
        { type: ReactionType.SAD, emoji: '😢', label: 'Sad' },
        { type: ReactionType.ANGRY, emoji: '😡', label: 'Angry' },
    ];

    async ngOnInit(): Promise<void> {
        await Promise.all([this.loadReactions(), this.loadComments()]);
    }
    private async loadReactions(): Promise<void> {
        this.reactionsLoading.set(true);
        try {
            const res = await firstValueFrom(this.reactionService.getSummary(this.article.uuid));
            if (res.data) this.reactionSummary.set(res.data);
        } catch {
        } finally {
            this.reactionsLoading.set(false);
        }
    }

    async toggleReaction(type: ReactionType): Promise<void> {
        if (!this.isAuthenticated()) {
            toast.info('Please login to react');
            return;
        }
        try {
            await firstValueFrom(
                this.reactionService.toggle({ articleUuid: this.article.uuid, reaction: type }),
            );
            await this.loadReactions();
        } catch {
            toast.error('Failed to react');
        }
    }

    getReactionCount(type: ReactionType): number {
        const counts = this.reactionSummary()?.countByType;
        return counts?.[type] || 0;
    }

    private async loadComments(): Promise<void> {
        this.commentsLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.articleService.getComments(this.article.uuid, {
                    page: 0,
                    size: 50,
                    sortBy: 'createdAt',
                    order: 'desc',
                }),
            );
            this.comments.set((res.data?.content as Comment[]) || []);
        } catch {
        } finally {
            this.commentsLoading.set(false);
        }
    }

    async submitComment(): Promise<void> {
        if (!this.isAuthenticated()) {
            toast.info('Please login to comment');
            return;
        }
        const content = this.newComment().trim();
        if (!content) return;

        this.submitting.set(true);
        try {
            await firstValueFrom(
                this.commentService.create({
                    articleUuid: this.article.uuid,
                    content,
                }),
            );
            this.newComment.set('');
            await this.loadComments();
            toast.success('Comment posted!');
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to post comment');
        } finally {
            this.submitting.set(false);
        }
    }

    close(): void {
        this.dialogRef.close();
    }
}
