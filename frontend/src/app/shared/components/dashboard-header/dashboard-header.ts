import { Component, computed, inject, input, output, signal } from '@angular/core';
import { NavigationService } from '../../../core/services/navigation-service';
import { ThemeService } from '../../../core/services/theme-service';
import { AuthStore } from '../../../core/auth/auth.store';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { MatRipple } from '@angular/material/core';
import { UserDropdown } from '../user-dropdown/user-dropdown';
import { MenuItem } from '../../../core/models/navigation.models';
import { BrandLogo } from '../brand-logo/brand-logo';

@Component({
    selector: 'app-dashboard-header',
    imports: [MatIcon, MatRipple, UserDropdown, BrandLogo],
    templateUrl: './dashboard-header.html',
    styles: ``,
})
export class DashboardHeader {
    readonly themeService = inject(ThemeService);
    readonly authStore = inject(AuthStore);
    readonly navService = inject(NavigationService);

    readonly sidebarToggle = output<void>();

    readonly showUserMenu = signal(false);

    readonly userMenuLinks = signal<MenuItem[]>([
        {
            label: 'My Profile',
            path: this.navService.dashboardHome() + '/profile',
            icon: 'person',
            roles: ['ADMIN', 'GUIDE', 'TOURIST'],
        },
        {
            label: 'Settings',
            path: this.navService.dashboardHome() + '/settings',
            icon: 'settings',
            roles: ['ADMIN', 'GUIDE', 'TOURIST'],
        },
    ]);

    closeAll(): void {
        this.showUserMenu.set(false);
    }
}
