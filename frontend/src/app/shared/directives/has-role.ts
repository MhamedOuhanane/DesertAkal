import { Directive, effect, inject, input, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthStore } from '../../core/auth/auth.store';
import { RoleEnum } from '../../core/enums/role.enum';

@Directive({
    selector: '[appHasRole]',
})
export class HasRole {
    private readonly authStore = inject(AuthStore);
    private readonly templateRef = inject(TemplateRef);
    private readonly viewContainer = inject(ViewContainerRef);

    appHasRole = input.required<RoleEnum | RoleEnum[]>();

    private isRendered = false;

    constructor() {
        effect(() => {
            const allowedRoles = this.normalizeRoles(this.appHasRole());
            const currentRole = this.authStore.userRole();
            const hasAccess = allowedRoles.includes(currentRole);

            if (hasAccess && !this.isRendered) {
                this.viewContainer.createEmbeddedView(this.templateRef);
                this.isRendered = true;
            } else if (!hasAccess && this.isRendered) {
                this.viewContainer.clear();
                this.isRendered = false;
            }
        });
    }

    private normalizeRoles(roles: RoleEnum | RoleEnum[]): RoleEnum[] {
        return Array.isArray(roles) ? roles : [roles];
    }
}
