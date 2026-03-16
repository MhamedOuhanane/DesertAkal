import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { UserService } from '../../../../core/services/user-service';
import { Pagination } from '../../../../core/models/response.models';
import { User, UserFilters } from '../../../../core/models/user.models';
import { StatusDialog } from '../../../../shared/components/status-dialog/status-dialog';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination';
import { DeleteDialog } from '../../../../shared/components/delete-dialog/delete-dialog';
import { AuthStore } from '../../../../core/auth/auth.store';

@Component({
    selector: 'app-user-list',
    imports: [FormsModule, DatePipe, StatusDialog, PaginationComponent, DeleteDialog],
    templateUrl: './user-list.html',
})
export class UserList {
    private userService = inject(UserService);
    private router = inject(Router);
    private authStore = inject(AuthStore);

    pagination = signal<Pagination<User> | null>(null);
    users = computed<User[]>(() => {
        const allUsers = this.pagination()?.content || [];
        const currentUser = this.authStore.user();

        if (!currentUser) return allUsers;

        return allUsers.filter((u) => u.uuid !== currentUser.uuid);
    });
    isLoading = signal(true);

    query = signal<UserFilters>({
        page: 0,
        size: 9,
        sortBy: 'lastLoginAt',
        order: 'desc',
        search: '',
        status: '',
        roleName: '',
    });

    showDeleteDialog = signal(false);
    userToDelete = signal<any | null>(null);
    isDeleting = signal(false);

    showStatusDialog = signal(false);
    userToUpdateStatus = signal<any | null>(null);
    newStatus = signal('');
    isUpdatingStatus = signal(false);

    private searchSubject = new Subject<string>();

    constructor() {
        this.searchSubject
            .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed())
            .subscribe((search) => {
                this.patchQuery({ search, page: 0 });
            });

        effect(() => {
            this.loadUsers();
        });
    }

    private patchQuery(patch: Partial<UserFilters>) {
        this.query.update((prev) => ({ ...prev, ...patch }));
    }

    async loadUsers(): Promise<void> {
        this.isLoading.set(true);
        try {
            const res = await firstValueFrom(this.userService.findAll(this.query()));
            if (res.data) {
                this.pagination.set(res.data);
            }
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load users');
        } finally {
            this.isLoading.set(false);
        }
    }

    onSearch(value: string): void {
        this.searchSubject.next(value);
    }

    onStatusFilter(status: string): void {
        this.patchQuery({ status, page: 0 });
        this.loadUsers();
    }

    onRoleFilter(roleName: string): void {
        this.patchQuery({ roleName, page: 0 });
        this.loadUsers();
    }

    onSort(field: string): void {
        const { sortBy, order } = this.query();
        const newOrder = sortBy === field && order === 'asc' ? 'desc' : 'asc';
        this.patchQuery({ sortBy: field, order: newOrder });
        this.loadUsers();
    }

    goToPage(page: number): void {
        this.patchQuery({ page });
        this.loadUsers();
    }

    viewUser(uuid: string): void {
        this.router.navigate(['/dashboard/users', uuid]);
    }

    openStatusDialog(user: User, event: Event): void {
        event.stopPropagation();
        this.userToUpdateStatus.set(user);
        this.newStatus.set(user.status);
        this.showStatusDialog.set(true);
    }

    async onConfirmStatusUpdate(newStatus: string): Promise<void> {
        const user = this.userToUpdateStatus();
        if (!user) return;

        this.isUpdatingStatus.set(true);
        try {
            await firstValueFrom(this.userService.updateStatus(user.uuid, newStatus));
            toast.success(`Status updated to ${newStatus}`);
            this.showStatusDialog.set(false);
            await this.loadUsers();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to update status');
        } finally {
            this.isUpdatingStatus.set(false);
        }
    }

    cancelStatusUpdate(): void {
        this.showStatusDialog.set(false);
        this.userToUpdateStatus.set(null);
    }

    openDeleteDialog(user: any, event: Event): void {
        event.stopPropagation();
        this.userToDelete.set(user);
        this.showDeleteDialog.set(true);
    }

    async confirmDelete(): Promise<void> {
        const user = this.userToDelete();
        if (!user) return;

        this.isDeleting.set(true);
        try {
            await firstValueFrom(this.userService.delete(user.uuid));
            toast.success('User deleted successfully');
            this.showDeleteDialog.set(false);
            this.userToDelete.set(null);
            await this.loadUsers();
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete user');
            this.showDeleteDialog.set(false);
            this.userToDelete.set(null);
        } finally {
            this.isDeleting.set(false);
        }
    }

    cancelDelete(): void {
        this.showDeleteDialog.set(false);
        this.userToDelete.set(null);
    }

    getInitials(firstName: string, lastName: string): string {
        return `${firstName?.charAt(0) || ''}${lastName?.charAt(0) || ''}`.toUpperCase();
    }

    getStatusConfig(status: string): { bg: string; text: string; dot: string } {
        switch (status) {
            case 'ACTIVE':
                return {
                    bg: 'bg-green-500/10',
                    text: 'text-green-600',
                    dot: 'bg-green-500',
                };
            case 'BANNED':
                return {
                    bg: 'bg-red-500/10',
                    text: 'text-red-600',
                    dot: 'bg-red-500',
                };
            case 'SUSPENDED':
                return {
                    bg: 'bg-orange-500/10',
                    text: 'text-orange-600',
                    dot: 'bg-orange-500',
                };
            default:
                return {
                    bg: 'bg-gray-500/10',
                    text: 'text-gray-500',
                    dot: 'bg-gray-400',
                };
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

    readonly statuses = ['ACTIVE', 'BANNED', 'SUSPENDED'];
    readonly roles = ['ADMIN', 'GUIDE', 'TOURIST'];
}
