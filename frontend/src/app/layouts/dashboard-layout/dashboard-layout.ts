import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-dashboard-layout',
    imports: [RouterOutlet],
    host: {
        class: 'block',
    },
    template: `
        <div class="flex h-screen overflow-hidden bg-main-bg text-primary">
            <aside
                [class.w-64]="isSidebarOpen()"
                [class.w-20]="!isSidebarOpen()"
                class="transition-all duration-300 border-r border-border bg-surface overflow-y-auto hidden md:block"
            >
                <!-- <app-sidebar [collapsed]="!isSidebarOpen()" /> -->
            </aside>

            <div class="flex flex-col flex-1 overflow-hidden">
                <header
                    class="h-16 border-b border-border bg-surface flex items-center px-6 justify-between shadow-sm"
                >
                    <button
                        (click)="toggleSidebar()"
                        class="p-2 cursor-pointer hover:bg-primary/10 rounded-lg transition-colors"
                    >
                        <span class="material-icons">{{ isSidebarOpen() ? 'close' : 'menu'}}</span>
                    </button>
                    <!-- <app-header [minimal]="true" />  -->
                </header>

                <main class="flex-1 overflow-y-auto p-6 animate-fade-in bg-main-bg">
                    <router-outlet />
                </main>
            </div>
        </div>
    `,
    styles: ``,
})
export class DashboardLayout {
    isSidebarOpen = signal(true);

    toggleSidebar() {
        this.isSidebarOpen.update((v) => !v);
    }
}
