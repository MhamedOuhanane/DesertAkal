import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth.store';
import { toast } from 'ngx-sonner';

export const authGuard: CanActivateFn = (route, state) => {
    const authStore = inject(AuthStore);
    const router = inject(Router);

    if (authStore.isAuthenticated()) {
        return true;
    }

    toast.error('Access Denied', {
        description: 'Please login to access this page.',
    });

    return router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url },
    });
};
