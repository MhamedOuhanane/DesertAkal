import { Component, inject, signal } from '@angular/core';
import { ThemeService } from '../../../core/services/theme-service';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
    selector: 'app-public-header',
    imports: [RouterLink, RouterLinkActive],
    templateUrl: './public-header.html',
    styleUrl: './public-header.scss',
})
export class PublicHeader {
    themeService = inject(ThemeService);
    mobileMenuOpen = signal(false);

    navLinks = [
        {
            path: '/',
            label: 'Home',
            exact: true,
            icon: 'M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z',
        },
        {
            path: '/tours',
            label: 'Tours',
            exact: false,
            icon: 'M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z',
        },
        {
            path: '/about',
            label: 'About',
            exact: false,
            icon: 'M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z',
        },
        {
            path: '/contact',
            label: 'Contact',
            exact: false,
            icon: 'M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z',
        },
    ];

    toggleMobileMenu(): void {
        this.mobileMenuOpen.update((v) => !v);
    }

    closeMobileMenu(): void {
        this.mobileMenuOpen.set(false);
    }
}
