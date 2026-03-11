import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('../../layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
        children: [
            {
                path: 'login',
                loadComponent: () => import('./login/login').then((m) => m.Login),
            },
            {
                path: 'register',
                loadComponent: () => import('./register/register').then((m) => m.Register),
            },
            {
                path: 'verify-email',
                loadComponent: () =>
                    import('./verify-email/verify-email').then((m) => m.VerifyEmail),
            },
            {
                path: 'confirm-email',
                loadComponent: () =>
                    import('./confirm-email/confirm-email').then((m) => m.ConfirmEmail),
            },
            {
                path: '',
                redirectTo: 'login',
                pathMatch: 'full',
            },
        ],
    },
];
