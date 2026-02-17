import { Component, computed, inject, input, output, signal } from '@angular/core';
import { NavigationService } from '../../../core/services/navigation-service';
import { ThemeService } from '../../../core/services/theme-service';
import { AuthStore } from '../../../core/auth/auth.store';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { MatRipple } from '@angular/material/core';

@Component({
    selector: 'app-dashboard-header',
    imports: [MatIcon, RouterLink, MatRipple],
    templateUrl: './dashboard-header.html',
    styles: ``,
})
export class DashboardHeader {
    readonly themeService = inject(ThemeService);
    readonly authStore = inject(AuthStore);
    readonly navService = inject(NavigationService);

    readonly sidebarToggle = output<void>();

    readonly showUserMenu = signal(false);

    closeAll(): void {
        this.showUserMenu.set(false);
    }

    logout(): void {
        this.closeAll();
        this.authStore.logout();
    }
}
