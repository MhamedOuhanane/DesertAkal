import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe, SlicePipe } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { Comment } from '../../../../core/models/comment.model';
import { UserFind } from '../../../../core/models/user.models';
import { UserService } from '../../../../core/services/user-service';
import { Article } from '../../../../core/models/article.models';

@Component({
    selector: 'app-user-detail',
    imports: [RouterLink, DatePipe, SlicePipe],
    templateUrl: './user-detail.html',
})
export class UserDetail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private userService = inject(UserService);

    user = signal<UserFind | null>(null);
    isLoading = signal(true);
    activeTab = signal<'articles' | 'comments'>('articles');

    articles = signal<Article[]>([]);
    comments = signal<Comment[]>([]);
    articlesLoading = signal(false);
    commentsLoading = signal(false);
    articlesTotalElements = signal(0);
    commentsTotalElements = signal(0);

    showStatusDialog = signal(false);
    newStatus = signal('');
    isUpdatingStatus = signal(false);

    showDeleteDialog = signal(false);
    isDeleting = signal(false);

    readonly statuses = ['ACTIVE', 'BANNED', 'SUSPENDED'];

    async ngOnInit(): Promise<void> {
        const uuid = this.route.snapshot.paramMap.get('uuid');
        if (!uuid) {
            this.router.navigate(['/dashboard/users']);
            return;
        }
        await this.loadUser(uuid);
    }

    private async loadUser(uuid: string): Promise<void> {
        try {
            const res = await firstValueFrom(this.userService.findOne(uuid));
            this.user.set(res.data);
            await this.loadArticles();
            await this.loadComments();
        } catch (err: any) {
            toast.error(err?.error?.message || 'User not found');
            this.router.navigate(['/dashboard/users']);
        } finally {
            this.isLoading.set(false);
        }
    }

    async loadArticles(): Promise<void> {
        if (!this.user()) return;
        this.articlesLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.userService.getArticles(this.user()!.uuid, { page: 0, size: 10 }),
            );
            this.articles.set(res.data?.content as Article[]);
            this.articlesTotalElements.set(res.data?.totalElements || 0);
        } catch {
        } finally {
            this.articlesLoading.set(false);
        }
    }

    async loadComments(): Promise<void> {
        if (!this.user()) return;
        this.commentsLoading.set(true);
        try {
            const res = await firstValueFrom(
                this.userService.getComments(this.user()!.uuid, { page: 0, size: 10 }),
            );
            this.comments.set(res.data?.content as Comment[]);
            this.commentsTotalElements.set(res.data?.totalElements || 0);
        } catch {
        } finally {
            this.commentsLoading.set(false);
        }
    }

    switchTab(tab: 'articles' | 'comments'): void {
        this.activeTab.set(tab);
    }

    openStatusDialog(): void {
        this.newStatus.set(this.user()!.status);
        this.showStatusDialog.set(true);
    }

    async confirmStatusUpdate(): Promise<void> {
        if (!this.user() || !this.newStatus()) return;
        this.isUpdatingStatus.set(true);
        try {
            const res = await firstValueFrom(
                this.userService.updateStatus(this.user()!.uuid, this.newStatus()),
            );
            this.user.set(res.data);
            toast.success(`Status updated to ${this.newStatus()}`);
            this.showStatusDialog.set(false);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to update status');
        } finally {
            this.isUpdatingStatus.set(false);
        }
    }

    openDeleteDialog(): void {
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        if (!this.user()) return;
        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.userService.delete(this.user()!.uuid));
            toast.success('User deleted successfully');
            this.router.navigate(['/dashboard/users']);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete user');
        } finally {
            this.isDeleting.set(false);
            this.showDeleteDialog.set(false);
        }
    }

    getInitials(firstName: string, lastName: string): string {
        return `${firstName?.charAt(0) || ''}${lastName?.charAt(0) || ''}`.toUpperCase();
    }

    getStatusConfig(status: string): { bg: string; text: string; dot: string } {
        switch (status) {
            case 'ACTIVE':
                return { bg: 'bg-green-500/10', text: 'text-green-600', dot: 'bg-green-500' };
            case 'BANNED':
                return { bg: 'bg-red-500/10', text: 'text-red-600', dot: 'bg-red-500' };
            case 'SUSPENDED':
                return { bg: 'bg-orange-500/10', text: 'text-orange-600', dot: 'bg-orange-500' };
            default:
                return { bg: 'bg-gray-500/10', text: 'text-gray-500', dot: 'bg-gray-400' };
        }
    }

    getRoleBadge(role: string): string {
        switch (role) {
            case 'ADMIN':
                return 'bg-purple-500/10 text-purple-600';
            case 'GUIDE':
                return 'bg-blue-500/10 text-blue-600';
            case 'TOURIST':
                return 'bg-teal-500/10 text-teal-600';
            default:
                return 'bg-gray-500/10 text-gray-500';
        }
    }
}
