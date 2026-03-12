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
                data: { breadcrumb: 'Tours' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./tours/tour-list/tour-list').then((m) => m.TourList),
                    },
                    {
                        path: 'create',
                        loadComponent: () =>
                            import('./tours/tour-form/tour-form').then((m) => m.TourForm),
                        data: { breadcrumb: 'Create' },
                    },
                    // {
                    //     path: ':uuid',
                    //     loadComponent: () =>
                    //         import('./tours/tour-detail/tour-detail').then((m) => m.TourDetail),
                    //     data: { breadcrumb: 'Details' },
                    // },
                    {
                        path: ':uuid/edit',
                        loadComponent: () =>
                            import('./tours/tour-form/tour-form').then((m) => m.TourForm),
                        data: { breadcrumb: 'Edit' },
                    },
                ],
            },
        ],
    },
];
