import { Component, computed, effect, inject, OnDestroy, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatIcon } from '@angular/material/icon';
import { MatRipple } from '@angular/material/core';
import { toast } from 'ngx-sonner';
import { AuthStore } from '../../../core/auth/auth.store';
import { Notification, NotificationFind } from '../../../core/models/notification.model';
import { NotificationService } from '../../../core/services/notification-service';

@Component({
    selector: 'app-notification-bell',
    imports: [DatePipe, MatIcon, MatRipple],
    templateUrl: './notification-bell.html',
})
export class NotificationBell implements OnDestroy {
    private authStore = inject(AuthStore);
    private notificationService = inject(NotificationService);
    private router = inject(Router);

    isOpen = signal(false);
    isLoading = signal(false);
    isLoadingMore = signal(false);
    notifications = signal<Notification[]>([]);
    totalElements = signal(0);
    currentPage = signal(0);
    selectedNotification = signal<NotificationFind | null>(null);

    unreadCount = computed(() => this.notifications().filter((n) => !n.seen).length);

    private pollingInterval: any;
    private readonly POLL_INTERVAL = 30000;
    private readonly PAGE_SIZE = 8;

    constructor() {
        effect(() => {
            const uuid = this.authStore.user()?.uuid;
            if (uuid) {
                this.loadNotifications();
                this.startPolling();
            }
        });
    }

    ngOnDestroy(): void {
        this.stopPolling();
    }

    private startPolling(): void {
        this.stopPolling();
        this.pollingInterval = setInterval(() => {
            this.loadNotifications(true);
        }, this.POLL_INTERVAL);
    }

    private stopPolling(): void {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }
    }

    async loadNotifications(silent = false): Promise<void> {
        const uuid = this.authStore.user()?.uuid;
        if (!uuid) return;

        if (!silent) this.isLoading.set(true);

        try {
            const res = await firstValueFrom(
                this.notificationService.findByUser(uuid, {
                    page: 0,
                    size: this.PAGE_SIZE,
                    sortBy: 'date',
                    order: 'desc',
                }),
            );
            if (res.data) {
                this.notifications.set((res.data.content as Notification[]) || []);
                this.totalElements.set(res.data.totalElements);
                this.currentPage.set(0);
            }
        } catch {
        } finally {
            if (!silent) this.isLoading.set(false);
        }
    }

    async loadMore(): Promise<void> {
        const uuid = this.authStore.user()?.uuid;
        if (!uuid) return;

        this.isLoadingMore.set(true);
        const nextPage = this.currentPage() + 1;

        try {
            const res = await firstValueFrom(
                this.notificationService.findByUser(uuid, {
                    page: nextPage,
                    size: this.PAGE_SIZE,
                    sortBy: 'date',
                    order: 'desc',
                }),
            );
            if (res.data) {
                const newNotifs = (res.data.content as Notification[]) || [];
                this.notifications.update((current) => [...current, ...newNotifs]);
                this.currentPage.set(nextPage);
            }
        } catch {
            toast.error('Failed to load more notifications');
        } finally {
            this.isLoadingMore.set(false);
        }
    }

    togglePanel(): void {
        if (!this.isOpen()) {
            this.loadNotifications();
        }
        this.isOpen.update((v) => !v);
    }

    closePanel(): void {
        this.isOpen.set(false);
    }

    async openNotification(notif: Notification): Promise<void> {
        try {
            const res = await firstValueFrom(this.notificationService.findOne(notif.uuid));
            this.selectedNotification.set(res.data!);

            this.notifications.update((list) =>
                list.map((n) => (n.uuid === notif.uuid ? { ...n, seen: true } : n)),
            );
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to load notification');
        }
    }

    closeDetail(): void {
        this.selectedNotification.set(null);
    }

    async deleteNotification(notif: Notification, event: Event): Promise<void> {
        event.stopPropagation();
        try {
            await firstValueFrom(this.notificationService.delete(notif.uuid));
            this.notifications.update((list) => list.filter((n) => n.uuid !== notif.uuid));
            this.totalElements.update((t) => t - 1);
            toast.success('Notification deleted');
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to delete');
        }
    }

    getNotifIcon(title: string): string {
        const t = title.toLowerCase();
        if (t.includes('booking') || t.includes('reservation')) return 'calendar_month';
        if (t.includes('payment') || t.includes('refund')) return 'payments';
        if (t.includes('cancel')) return 'cancel';
        if (t.includes('confirm')) return 'check_circle';
        if (t.includes('assign') || t.includes('tour')) return 'map';
        if (t.includes('review')) return 'rate_review';
        if (t.includes('comment')) return 'chat_bubble';
        if (t.includes('schedule') || t.includes('date')) return 'schedule';
        return 'notifications';
    }

    getTimeAgo(date: string | Date): string {
        const now = new Date();
        const d = new Date(date);
        const diff = now.getTime() - d.getTime();

        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return 'Just now';
        if (minutes < 60) return `${minutes}m ago`;
        if (hours < 24) return `${hours}h ago`;
        if (days < 7) return `${days}d ago`;
        return d.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
        });
    }
}
