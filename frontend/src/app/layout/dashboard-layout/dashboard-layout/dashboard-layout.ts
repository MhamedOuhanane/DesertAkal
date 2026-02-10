import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-dashboard-layout',
    imports: [RouterOutlet],
    host: {
        class: 'block h-full',
    },
    templateUrl: './dashboard-layout.html',
    styleUrl: './dashboard-layout.scss',
})
export class DashboardLayout {
    isSidebarOpen = signal(true);

    toggleSidebar() {
        this.isSidebarOpen.update((v) => !v);
    }
}
