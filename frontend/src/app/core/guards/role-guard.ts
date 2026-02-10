import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../auth/auth.store';
import { toast } from 'ngx-sonner';

export const roleGuard: CanActivateFn = (route, state) => {
    const authStore = inject(AuthStore);
    const router = inject(Router);

    const expectedRoles = route.data['roles'] as Array<string>;
    const userRole = authStore.userRole();

    if (expectedRoles.includes(userRole!)) {
        return true;
    }

    toast.error('Permission Denied', {
        description: "You don't have the required permissions for this area.",
    });

    return router.createUrlTree(['/unauthorized']);
};
