import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../auth/auth.store';

export const homeGuard: CanActivateFn = (route, state) => {
    const store = inject(AuthStore);
    const router = inject(Router);

    const role = store.userRole();
    const isAuth = store.isAuthenticated();

    if (isAuth) {
        if (role === 'ADMIN') {
            return router.createUrlTree(['/dashboard']);
        } 
        if (role === 'GUIDE') {
            return router.createUrlTree(['/dashboard/guide']);
        }
        return router.createUrlTree(['/']);
    }


    return true;
};
