import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../auth/auth.store';

export const guestGuard: CanActivateFn = (route, state) => {
    const store = inject(AuthStore);
    const router = inject(Router);

    if (store.isAuthenticated()) {
        return router.createUrlTree(['/']);
    }

    return true;
};
