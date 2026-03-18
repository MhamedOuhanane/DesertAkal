import { Routes } from '@angular/router';

export const TOURIST_ROUTES: Routes = [
    {
        path: 'dashboard',
        loadComponent: () => import('./tourist-dashboard/tourist-dashboard').then((m) => m.TouristDashboard),
    },
    {
        path: 'dashboard/profile',
        loadComponent: () => import('../../shared/pages/profile/profile').then((m) => m.Profile),
        data: { breadcrumb: 'Profile' },
    },
    {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
    }
];
