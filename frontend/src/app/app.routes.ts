import { Routes } from '@angular/router';
import { homeGuard } from './core/guards/home-guard';

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
        canActivate: [homeGuard],
        loadChildren: () => import('./features/auth/auth.routes').then(r => r.AUTH_ROUTES),
    },

    {
        path: 'dashboard',
        // canActivate: [authGuard, roleGuard],
        // data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/admin/admin.routes').then((r) => r.ADMIN_ROUTES),
    },

    {
        path: 'guide',
        // canActivate: [authGuard, roleGuard],
        data: { roles: ['GUIDE'] },
        loadChildren: () => import('./features/guide/guide.routes').then((r) => r.GUIDE_ROUTES),
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
