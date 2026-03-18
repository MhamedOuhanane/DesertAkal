import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../auth/auth.store';

export const homeGuard: CanActivateFn = (route, state) => {
    const store = inject(AuthStore);
    const router = inject(Router);

    const role = store.userRole();

    if (store.isAuthenticated()) {
        if (role === 'ADMIN') return router.createUrlTree(['/dashboard']);
        if (role === 'GUIDE') return router.createUrlTree(['/guide/dashboard']);
    }

    return true;
};
