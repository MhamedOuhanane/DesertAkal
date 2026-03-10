import { Component, computed, inject, input, output, signal } from '@angular/core';
import { AuthStore } from '../../../core/auth/auth.store';
import { NavigationService } from '../../../core/services/navigation-service';
import { MenuItem } from '../../../core/models/navigation.models';
import { MatIcon } from '@angular/material/icon';
import { HasRole } from '../../directives';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatRipple } from '@angular/material/core';
import { SignOutButton } from '../sign-out-button/sign-out-button';
import { RoleEnum } from '../../../core/enums/role.enum';

@Component({
    selector: 'app-user-dropdown',
    imports: [MatIcon, HasRole, RouterLink, MatRipple, RouterLinkActive, SignOutButton],
    templateUrl: './user-dropdown.html',
    styles: `
        .ring-active {
            --ring-color: color-mix(in srgb, var(--primary-color) 30%, transparent);
            box-shadow:
                0 0 0 2px var(--surface-color),
                0 0 0 4px var(--ring-color),
                0 0 10px color-mix(in srgb, var(--primary-color) 20%, transparent);
        }
    `,
})
export class UserDropdown {
    readonly authStore = inject(AuthStore);
    readonly navService = inject(NavigationService);
    protected readonly RoleEnum = RoleEnum;

    readonly userMenuLinks = input.required<MenuItem[]>();
    readonly loggedOut = output<void>();

    readonly isOpen = signal(false);

    readonly userName = computed(() => {
        const u = this.authStore.user();
        return u ? u.fullName : 'Guest';
    });

    readonly userInitials = computed(() => {
        const u = this.authStore.user();
        if (!u || !u.fullName) return 'G';

        const names = u.fullName.trim().split(/\s+/);

        if (names.length >= 2) {
            const firstInitial = names[0].charAt(0);
            const lastInitial = names[names.length - 1].charAt(0);
            return `${firstInitial}${lastInitial}`.toUpperCase();
        }

        return u.fullName.substring(0, 2).toUpperCase();
    });

    readonly roleBadgeClass = this.navService.roleBadgeClass;
    readonly roleTextClass = this.navService.roleTextClass;

    readonly hasCustomPhoto = computed(() => {
        const photo = this.authStore.userPhoto();
        return !!photo && !photo.includes('default-profile');
    });

    toggle(): void {
        this.isOpen.update((v) => !v);
    }

    close(): void {
        this.isOpen.set(false);
    }

    logout(): void {
        this.close();
        this.authStore.logout();
        this.loggedOut.emit();
    }
}
