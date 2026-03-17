import { Routes } from '@angular/router';

export const GUIDE_ROUTES: Routes = [
    {
        path: 'profile',
        loadComponent: () => import('../../shared/pages/profile/profile').then((m) => m.Profile),
        data: { breadcrumb: 'Profile' },
    },
];
