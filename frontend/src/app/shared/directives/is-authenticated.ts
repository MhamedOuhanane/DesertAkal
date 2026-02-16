import { Directive, effect, inject, input, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthStore } from '../../core/auth/auth.store';

@Directive({
  selector: '[appIsAuthenticated]',
})
export class IsAuthenticated {
private readonly authStore = inject(AuthStore);
  private readonly templateRef = inject(TemplateRef);
  private readonly viewContainer = inject(ViewContainerRef);
    appIsAuthenticated = input<boolean>(true);

  private isRendered = false;

  constructor() {
    effect(() => {
      const isAuth = this.authStore.isAuthenticated();
      const shouldShow = this.appIsAuthenticated() ? isAuth : !isAuth;

      if (shouldShow && !this.isRendered) {
        this.viewContainer.createEmbeddedView(this.templateRef);
        this.isRendered = true;
      } else if (!shouldShow && this.isRendered) {
        this.viewContainer.clear();
        this.isRendered = false;
      }
    });
    
}
}
