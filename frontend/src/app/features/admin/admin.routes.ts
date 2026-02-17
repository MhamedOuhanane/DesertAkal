import { Routes } from '@angular/router';
import { DashboardLayout } from '../../layouts/dashboard-layout/dashboard-layout';

export const ADMIN_ROUTES: Routes = [
    {
        path: '',
        component: DashboardLayout,
        data: { breadcrumb: 'Dashboard' },
        children: [
            {
                path: '',
                loadComponent: () => import('./dashboard/dashboard').then((m) => m.Dashboard),
            },
            {
                path: 'tours',
                loadComponent: () =>
                    import('./tours-management/tours-management').then((m) => m.ToursManagement),
                data: { breadcrumb: 'Tours Management' },
            },
        ],
    },
];
