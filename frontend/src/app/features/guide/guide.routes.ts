import { Routes } from '@angular/router';

export const GUIDE_ROUTES: Routes = [
    {
        path: 'dashboard',
        loadComponent: () => import('./guide-dashboard/guide-dashboard').then((m) => m.GuideDashboard),
    },
    {
        path: 'profile',
        loadComponent: () => import('../../shared/pages/profile/profile').then((m) => m.Profile),
        data: { breadcrumb: 'Profile' },
    },
    {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
    },
];
