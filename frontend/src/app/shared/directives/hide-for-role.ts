import { Directive, effect, inject, input, signal, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthStore } from '../../core/auth/auth.store';
import { UserRole } from '../../core/models/user.models';

@Directive({
  selector: '[appHideForRole]',
})
export class HideForRole {
    private readonly authStore = inject(AuthStore);
    private readonly templateRef = inject(TemplateRef);
    private readonly viewContainer = inject(ViewContainerRef);

    appHideForRole = input.required<UserRole | UserRole[]>();

    isRendered = signal(false);

  constructor() {
    effect(() => {
        const currentRole = this.authStore.userRole();
        const allowedRoles = this.normalizeRoles(this.appHideForRole());
        const shouldHide  = allowedRoles.includes(currentRole);

        if (!shouldHide && !this.isRendered()) {
            this.viewContainer.createEmbeddedView(this.templateRef);
            this.isRendered.set(true);
        } else if (shouldHide && this.isRendered()) {
            this.viewContainer.clear();
            this.isRendered.set(false);
        } 
    })
   }

   private normalizeRoles(roles: UserRole | UserRole[]): UserRole[] {
    return Array.isArray(roles) ? roles : [roles];
  }

}
