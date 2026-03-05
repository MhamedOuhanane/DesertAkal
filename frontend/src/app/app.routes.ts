import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth-guard';
import { roleGuard } from './core/guards/role-guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./layouts/main-layout/main-layout').then((m) => m.MainLayout),
        canActivate: [roleGuard],
        data: { roles: ['VISITOR', 'TOURIST'] },
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
