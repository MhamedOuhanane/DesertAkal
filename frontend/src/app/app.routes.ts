import { Routes } from '@angular/router';
import { homeGuard } from './core/guards/home-guard';
import { authGuard } from './core/auth/auth-guard';
import { roleGuard } from './core/guards/role-guard';
import { guestGuard } from './core/guards/guest-guard';

export const routes: Routes = [
    {
        path: '',
        canActivate: [homeGuard],
        loadComponent: () => import('./layouts/main-layout/main-layout').then((m) => m.MainLayout),
        children: [
            {
                path: '',
                loadComponent: () => import('./features/home/home').then((m) => m.Home),
            },
            {
                path: 'tours',
                loadComponent: () => import('./features/tour/tour').then((m) => m.Tour),
            },
        ],
    },

    {
        path: 'auth',
        canActivate: [guestGuard],
        loadChildren: () => import('./features/auth/auth.routes').then((r) => r.AUTH_ROUTES),
    },

    {
        path: 'dashboard',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN', 'GUIDE', 'TOURIST'] },
        children: [
            
            {
                path: '',
                canActivate: [roleGuard],
                data: { roles: ['ADMIN'] },
                loadChildren: () => import('./features/admin/admin.routes').then((r) => r.ADMIN_ROUTES),
            },
            {
                path: 'profile',
                loadComponent: () =>
                    import('./shared/pages/profile/profile').then((m) => m.Profile),
                data: { breadcrumb: 'Profile' },
            },
        ],
    },

    {
        path: 'guide',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['GUIDE'] },
        loadChildren: () => import('./features/guide/guide.routes').then((r) => r.GUIDE_ROUTES),
    },

    {
        path: 'oauth2/redirect',
        loadComponent: () =>
            import('./shared/components/oauth2-redirect/oauth2-redirect').then(
                (m) => m.OAuth2RedirectComponent,
            ),
    },

    {
        path: 'reservations/verify/:uuid',
        loadComponent: () =>
            import('./features/reservations/reservation-verify/reservation-verify').then(
                (m) => m.ReservationVerify,
            ),
        data: { breadcrumb: 'Verify Reservation' },
    },

    {
        path: 'unauthorized',
        loadComponent: () =>
            import('./shared/pages/unauthorized/unauthorized').then((m) => m.Unauthorized),
    },

    {
        path: '**',
        loadComponent: () => import('./shared/pages/not-found/not-found').then((m) => m.NotFound),
    },
];
