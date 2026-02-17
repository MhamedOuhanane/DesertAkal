import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../../shared/components/sidebar/sidebar';
import { Breadcrumb } from '../../shared/components/breadcrumb/breadcrumb';
import { MatIcon } from '@angular/material/icon';
import { DashboardHeader } from '../../shared/components/dashboard-header/dashboard-header';

@Component({
    selector: 'app-dashboard-layout',
    imports: [RouterOutlet, Sidebar, Breadcrumb, MatIcon, DashboardHeader],
    host: {
        class: 'block',
    },
    template: `
        <div class="flex min-h-screen bg-main-bg">
            <!-- Sidebar -->
            <app-sidebar
                [collapsed]="sidebarCollapsed()"
                [mobileOpen]="sidebarMobileOpen()"
                (toggleCollapse)="toggleCollapse()"
                (closeMobile)="sidebarMobileOpen.set(false)"
            />

            <div
                class="flex flex-1 flex-col overflow-hidden transition-all
                duration-300"
            >
                <app-dashboard-header (sidebarToggle)="toggleMobileSidebar()" />

                <main class="flex-1 overflow-y-auto p-4 sm:p-6">
                    <app-breadcrumb />
                    <router-outlet />
                </main>
            </div>
        </div>
    `,
    styles: ``,
})
export class DashboardLayout {
    sidebarCollapsed = signal(false);
    sidebarMobileOpen = signal(false);

    toggleCollapse(): void {
        this.sidebarCollapsed.update((v) => !v);
        if (this.sidebarMobileOpen()) {
            this.sidebarCollapsed.set(false);
            this.sidebarMobileOpen.set(false);
        }
    }

    toggleMobileSidebar(): void {
        this.sidebarMobileOpen.update((v) => !v);
    }
}
