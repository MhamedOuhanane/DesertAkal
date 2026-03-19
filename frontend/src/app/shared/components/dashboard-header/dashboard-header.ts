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
import { RoleEnum } from '../../../core/enums/role.enum';
import { BreakpointObserver } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { NotificationBell } from '../notification-bell/notification-bell';

@Component({
    selector: 'app-dashboard-header',
    imports: [MatIcon, MatRipple, UserDropdown, BrandLogo, NotificationBell],
    templateUrl: './dashboard-header.html',
    styles: ``,
})
export class DashboardHeader {
    readonly themeService = inject(ThemeService);
    readonly authStore = inject(AuthStore);
    readonly navService = inject(NavigationService);
    private breakpointObserver = inject(BreakpointObserver);

    readonly sidebarToggle = output<void>();

    readonly showUserMenu = signal(false);

    readonly userMenuLinks = signal<MenuItem[]>([
        {
            label: 'My Profile',
            path: this.navService.dashboardHome() + '/profile',
            icon: 'person',
            roles: [RoleEnum.ADMIN, RoleEnum.GUIDE, RoleEnum.TOURIST],
        },
        {
            label: 'Settings',
            path: this.navService.dashboardHome() + '/settings',
            icon: 'settings',
            roles: [RoleEnum.ADMIN, RoleEnum.GUIDE, RoleEnum.TOURIST],
        },
    ]);

    readonly isWeb = toSignal(
        this.breakpointObserver.observe('(min-width: 768px)').pipe(map((result) => result.matches)),
        { initialValue: true },
    );

    closeAll(): void {
        this.showUserMenu.set(false);
    }
}
