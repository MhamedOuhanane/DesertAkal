import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ThemeToggle } from '../theme-toggle/theme-toggle';
import { NavigationService } from '../../../core/services/navigation-service';
import { IsAuthenticated } from '../../directives';
import { MobileMenu } from '../mobile-menu/mobile-menu';
import { UserDropdown } from '../user-dropdown/user-dropdown';

@Component({
    selector: 'app-public-header',
    imports: [RouterLink, RouterLinkActive, ThemeToggle, IsAuthenticated, MobileMenu, UserDropdown],
    templateUrl: './public-header.html',
    styleUrl: './public-header.scss',
})
export class PublicHeader {
    private readonly navService = inject(NavigationService);

    mobileMenuOpen = signal(false);

    readonly userMenuLinks = this.navService.filteredUserMenuLinks;

    readonly navLinks = this.navService.filteredPublicLinks();

    constructor() {
        effect(() => {
            if (this.mobileMenuOpen()) {
                document.body.style.overflow = 'hidden';
                document.body.style.paddingRight = '0px';
            } else {
                document.body.style.overflow = 'auto';
            }
        });
    }

    toggleMobileMenu(): void {
        this.mobileMenuOpen.update((v) => !v);
    }

    closeMobileMenu(): void {
        this.mobileMenuOpen.set(false);
    }
}
