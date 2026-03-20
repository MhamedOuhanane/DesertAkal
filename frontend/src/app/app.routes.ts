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
        loadChildren: () => import('./features/public/public.routes').then(r => r.PUBLIC_ROUTES),
    },

    {
        path: 'auth',
        canActivate: [guestGuard],
        loadChildren: () => import('./features/auth/auth.routes').then((r) => r.AUTH_ROUTES),
    },

    {
        path: '',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN', 'GUIDE', 'TOURIST'] },
        loadChildren: () => import('./features/dashboard.routes').then((r) => r.DASHBOARD_ROUTES),
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
