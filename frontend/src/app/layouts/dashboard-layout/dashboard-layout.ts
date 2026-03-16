import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../../shared/components/sidebar/sidebar';
import { Breadcrumb } from '../../shared/components/breadcrumb/breadcrumb';
import { DashboardHeader } from '../../shared/components/dashboard-header/dashboard-header';

@Component({
    selector: 'app-dashboard-layout',
    imports: [RouterOutlet, Sidebar, Breadcrumb, DashboardHeader],
    host: {
        class: 'block',
    },
    template: `
        <div class="flex min-h-screen bg-main-bg">
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

                <main class="flex-1 p-6 md:py-2 md:px-4 mt-16 ">
                    <app-breadcrumb />
                    <div class="md:h-[calc(100vh-120px)] md:overflow-y-auto md:pr-2 md:custom-scrollbar">
                        <router-outlet />
                    </div>
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
