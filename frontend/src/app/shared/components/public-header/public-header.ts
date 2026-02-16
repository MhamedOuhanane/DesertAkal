import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ThemeToggle } from '../theme-toggle/theme-toggle';
import { NavigationService } from '../../../core/services/navigation-service';
import { IsAuthenticated } from '../../directives';

@Component({
    selector: 'app-public-header',
    imports: [RouterLink, RouterLinkActive, ThemeToggle, IsAuthenticated],
    templateUrl: './public-header.html',
    styleUrl: './public-header.scss',
})
export class PublicHeader {
    private readonly navService = inject(NavigationService);

    mobileMenuOpen = signal(false);

    readonly navLinks = this.navService.filteredPublicLinks;

    toggleMobileMenu(): void {
        this.mobileMenuOpen.update((v) => !v);
    }

    closeMobileMenu(): void {
        this.mobileMenuOpen.set(false);
    }
}
