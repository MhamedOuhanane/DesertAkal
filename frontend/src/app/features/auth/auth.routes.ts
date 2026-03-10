import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('../../layouts/auth-layout/auth-layout').then((m) => m.AuthLayout),
        children: [
            {
                path: 'login',
                loadComponent: () => import('./login/login').then(m => m.Login),
            },
            // {
            //     path: 'register',
            //     loadComponent: () =>
            //         import('./register/register').then((m) => m.Register),
            // },
            // {
            //     path: 'verify-email',
            //     loadComponent: () =>
            //         import('./verify-email/verify-email').then((m) => m.VerifyEmail),
            // },
            // {
            //     path: 'forgot-password',
            //     loadComponent: () =>
            //         import('./forgot-password/forgot-password').then((m) => m.ForgotPassword),
            // },
            {
                path: '',
                redirectTo: 'login',
                pathMatch: 'full',
            }
        ],
    },
];
