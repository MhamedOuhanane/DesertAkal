import { Component, computed, inject, input, output } from '@angular/core';
import { IsAuthenticated } from '../../directives';
import { PublicNavLink } from '../../../core/models/navigation.models';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { AuthStore } from '../../../core/auth/auth.store';
import { NavigationService } from '../../../core/services/navigation-service';

@Component({
    selector: 'app-mobile-menu',
    imports: [IsAuthenticated, RouterLink, MatIcon, RouterLinkActive],
    templateUrl: './mobile-menu.html',
    styles: `
        .animate-mobile-popup {
            animation: popup 0.3s cubic-bezier(0.16, 1, 0.3, 1);
        }
        @keyframes popup {
            from {
                opacity: 0;
                transform: translateY(-10px) scale(0.95);
            }
            to {
                opacity: 1;
                transform: translateY(0) scale(1);
            }
        }
    `,
})
export class MobileMenu {
    readonly authStore = inject(AuthStore);
    readonly navService = inject(NavigationService);

    readonly links = input.required<PublicNavLink[]>();
    readonly close = output<void>();

    readonly userName = computed(() => this.authStore.user()?.fullName);

    readonly userInitials = computed(() => {
        const u = this.authStore.user();
        if (!u) return 'G';
        return u.fullName.charAt(1).toUpperCase();
    });

    readonly hasCustomPhoto = computed(() => {
        const photo = this.authStore.userPhoto();
        return !!photo && !photo.includes('default-profile');
    });

    readonly roleBadgeClass = this.navService.roleBadgeClass;

    onNavigate(): void {
        this.close.emit();
    }

    logout(): void {
        this.authStore.logout();
        this.close.emit();
    }
}
