import { computed, inject, Injectable } from '@angular/core';
import { AuthStore } from '../auth/auth.store';
import { UserRole } from '../models/user.models';
import { MenuGroup, MenuItem, PublicNavLink } from '../models/navigation.models';
import { NAV_CONFIG } from './navigation.config';
import { group } from 'console';

@Injectable({
    providedIn: 'root',
})
export class NavigationService {
    private readonly authStore = inject(AuthStore);

    readonly currentRole = computed<UserRole>(() => this.authStore.userRole());

    readonly isAuth = computed<boolean>(() => this.authStore.isAuthenticated());

    readonly isAdmin = computed<boolean>(() => this.currentRole() === 'ADMIN');
    readonly isGuide = computed<boolean>(() => this.currentRole() === 'GUIDE');
    readonly isTourist = computed<boolean>(() => this.currentRole() === 'TOURIST');
    readonly isVisitor = computed<boolean>(() => this.currentRole() === 'VISITOR');

    readonly filteredPublicLinks = computed<PublicNavLink[]>(() => {
        const role = this.currentRole();
        const authenticated = this.isAuth();

        return NAV_CONFIG.PUBLIC_LINKS.filter((link) => {
            if (!link.roles.includes(role)) return false;
            if (link.requiresAuth && !authenticated) return false;
            return true;
        });
    });

    readonly filteredUserMenuLinks = computed<MenuItem[]>(() => {
        const role = this.currentRole();

        return NAV_CONFIG.USER_MENU_LINKS.filter((item) => item.roles.includes(role));
    });

    readonly filteredSidebarGroups = computed<MenuGroup[]>(() => {
        const role = this.currentRole();

        return NAV_CONFIG.SIDEBAR_GROUPS.filter((group) => group.roles.includes(role))
            .map((group) => ({
                ...group,
                items: group.items.filter((item) => item.roles.includes(role)),
            }))
            .filter((group) => group.items.length > 0);
    });

    readonly filteredSidebarItems = computed<MenuItem[]>(() => {
        const role = this.currentRole();

        return NAV_CONFIG.SIDEBAR_GROUPS.flatMap((group) => group.items).filter((item) =>
            item.roles.includes(role),
        );
    });

    readonly totalSidebarItems = computed<number>(() => this.filteredSidebarItems().length);

    readonly dashboardHome = computed<string>(() => {
        switch (this.currentRole()) {
            case 'ADMIN':
                return '/dashboard';
            case 'GUIDE':
                return '/guide/dashboard';
            case 'TOURIST':
                return '/tourist/dashboard';
            default:
                return '/';
        }
    });

    searchSidebarItems(query: string): MenuItem[] {
        if (!query.trim()) return [];
        const q = query.toLowerCase();

        return this.filteredSidebarItems().filter((item) => item.label.toLowerCase().includes(q));
    }

    canAccess(path: string): boolean {
        const allItems = NAV_CONFIG.SIDEBAR_GROUPS.flatMap((g) => g.items);

        const item = allItems.find((i) => i.path === path);
        if (!item) return true;

        return item.roles.includes(this.currentRole());
    }

    readonly roleTextClass = computed(() => {
        switch (this.currentRole()) {
            case 'ADMIN':
                return 'text-error';
            case 'GUIDE':
                return 'text-info';
            case 'TOURIST':
                return 'text-success';
            default:
                return 'text-primary';
        }
    });

    readonly roleBadgeClass = computed(() => {
        switch (this.currentRole()) {
            case 'ADMIN':
                return 'bg-error/10 text-error';
            case 'GUIDE':
                return 'bg-info/10 text-info';
            case 'TOURIST':
                return 'bg-success/10 text-success';
            default:
                return 'bg-primary/10 text-primary';
        }
    });
}
