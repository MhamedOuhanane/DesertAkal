import { Routes } from '@angular/router';

export const GUIDE_ROUTES: Routes = [
    {
        path: 'dashboard',
        loadComponent: () =>
            import('./guide-dashboard/guide-dashboard').then((m) => m.GuideDashboard),
    },
    {
        path: 'dashboard/assignments',
        data: { breadcrumb: 'Assignments' },
        children: [
            {
                path: '',
                loadComponent: () =>
                    import('./my-assignments/my-assignments').then((m) => m.MyAssignments),
            },
            {
                path: ':uuid',
                loadComponent: () =>
                    import('../../shared/pages/reservation-detail/reservation-detail').then(
                        (m) => m.ReservationDetail,
                    ),
                data: { breadcrumb: 'Details' },
            },
        ],
    },
    {
        path: 'dashboard/tours',
        loadComponent: () => import('./my-tours/my-tours').then((m) => m.MyTours),
        data: { breadcrumb: 'My Tours' },
    },
    {
        path: 'dashboard/schedule',
        loadComponent: () => import('./my-schedule/my-schedule').then((m) => m.MySchedule),
        data: { breadcrumb: 'Schedule' },
    },
    {
        path: 'dashboard/reviews',
        loadComponent: () => import('./guide-reviews/guide-reviews').then((m) => m.GuideReviews),
        data: { breadcrumb: 'Reviews' },
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
    },
];
