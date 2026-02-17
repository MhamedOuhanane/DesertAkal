import { Component, computed, inject, input, output, signal } from '@angular/core';
import { NavigationService } from '../../../core/services/navigation-service';
import { MenuGroup } from '../../../core/models/navigation.models';
import { MatIcon } from '@angular/material/icon';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatRippleModule } from '@angular/material/core';
import { BrandLogo } from '../brand-logo/brand-logo';

@Component({
    selector: 'app-sidebar',
    imports: [MatIcon, RouterLink, RouterLinkActive, MatTooltipModule, MatRippleModule, BrandLogo],
    host: {
        class: 'block',
    },
    templateUrl: './sidebar.html',
    styleUrl: './sidebar.scss',
})
export class Sidebar {
    private readonly navService = inject(NavigationService);

    readonly menuGroups = computed<MenuGroup[]>(() => this.navService.filteredSidebarGroups());
    readonly pathHome = computed<string>(() => this.navService.dashboardHome());

    collapsed = input<boolean>(false);
    mobileOpen = input<boolean>(false);
    toggleCollapse = output<void>();
    closeMobile = output<void>();

    isExpanded = computed(() => !this.collapsed() || this.mobileOpen());

    onNavigate(): void {
        if (this.mobileOpen()) this.closeMobile.emit();
    }

    searchQuery = signal('');

    readonly searchResults = computed(() => {
        return this.navService.searchSidebarItems(this.searchQuery());
    });

    onSearch(event: Event) {
        const value = (event.target as HTMLInputElement).value;
        this.searchQuery.set(value);
    }

    clearSearch() {
        this.searchQuery.set('');
    }

    onSearchNavigate(): void {
        this.clearSearch();
        this.onNavigate();
    }
}
