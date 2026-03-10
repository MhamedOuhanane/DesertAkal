import { Component, effect, inject, PLATFORM_ID, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ThemeToggle } from '../theme-toggle/theme-toggle';
import { NavigationService } from '../../../core/services/navigation-service';
import { IsAuthenticated } from '../../directives';
import { MobileMenu } from '../mobile-menu/mobile-menu';
import { UserDropdown } from '../user-dropdown/user-dropdown';
import { BrandLogo } from '../brand-logo/brand-logo';
import { BreakpointObserver } from '@angular/cdk/layout';
import { map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { isPlatformBrowser } from '@angular/common';
@Component({
    selector: 'app-public-header',
    imports: [
        RouterLink,
        RouterLinkActive,
        ThemeToggle,
        IsAuthenticated,
        MobileMenu,
        UserDropdown,
        BrandLogo,
    ],
    templateUrl: './public-header.html',
    styleUrl: './public-header.scss',
})
export class PublicHeader {
    private readonly navService = inject(NavigationService);
    private readonly breakpointObserver = inject(BreakpointObserver);
    private readonly platformId = inject(PLATFORM_ID);

    mobileMenuOpen = signal(false);

    readonly isWeb = toSignal(
        this.breakpointObserver.observe('(min-width: 768px)').pipe(map((result) => result.matches)),
        { initialValue: true },
    );

    readonly userMenuLinks = this.navService.filteredUserMenuLinks;

    readonly navLinks = this.navService.filteredPublicLinks();

    constructor() {
        effect(() => {
            if (isPlatformBrowser(this.platformId)) {
                if (this.mobileMenuOpen()) {
                    document.body.style.overflow = 'hidden';
                    document.body.style.paddingRight = '0px';
                } else {
                    document.body.style.overflow = 'auto';
                }
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
