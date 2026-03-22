import { Routes } from '@angular/router';

export const TOURIST_ROUTES: Routes = [
    {
        path: 'dashboard',
        loadComponent: () =>
            import('./tourist-dashboard/tourist-dashboard').then((m) => m.TouristDashboard),
    },
    {
        path: 'dashboard/bookings',
        children: [
            {
                path: '',
                loadComponent: () => import('./my-bookings/my-bookings').then((m) => m.MyBookings),
            },
            {
                path: 'new',
                loadComponent: () =>
                    import('./reservation-flow/reservation-form/reservation-form').then(
                        (m) => m.ReservationForm,
                    ),
                data: { breadcrumb: 'New Booking' },
            },
            {
                path: ':uuid',
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('../../shared/pages/reservation-detail/reservation-detail').then(
                                (m) => m.ReservationDetail,
                            ),
                        data: { breadcrumb: 'Details' },
                    },
                    {
                        path: 'pay',
                        loadComponent: () =>
                            import('./reservation-flow/reservation-pay/reservation-pay').then(
                                (m) => m.ReservationPay,
                            ),
                        data: { breadcrumb: 'Pay Booking' },
                    },
                ],
            },
        ],
    },
    {
        path: 'dashboard/payments',
        loadComponent: () => import('./my-payments/my-payments').then((m) => m.MyPayments),
        data: { breadcrumb: 'Payments' },
    },
    {
        path: 'dashboard/posts',
        children: [
            { path: '', loadComponent: () => import('./my-posts/my-posts').then((m) => m.MyPosts) },
            {
                path: 'create',
                loadComponent: () =>
                    import('./my-posts/post-form/post-form').then((m) => m.PostForm),
                data: { breadcrumb: 'New Post' },
            },
            {
                path: ':uuid/edit',
                loadComponent: () =>
                    import('./my-posts/post-form/post-form').then((m) => m.PostForm),
                data: { breadcrumb: 'Edit' },
            },
        ],
    },
    {
        path: 'dashboard/reviews',
        loadComponent: () => import('./my-reviews/my-reviews').then((m) => m.MyReviews),
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
