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
                    {
                        path: ':uuid',
                        children: [
                            {
                                path: '',
                                loadComponent: () =>
                                    import('./tours/tour-detail/tour-detail').then(
                                        (m) => m.TourDetail,
                                    ),
                                data: { breadcrumb: 'Details' },
                            },
                            {
                                path: 'edit',
                                loadComponent: () =>
                                    import('./tours/tour-form/tour-form').then((m) => m.TourForm),
                                data: { breadcrumb: 'Edit' },
                            },
                        ],
                    },
                ],
            },

            {
                path: 'guides',
                data: { breadcrumb: 'Guides' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./guides/guide-list/guide-list').then((m) => m.GuideList),
                    },
                    {
                        path: 'create',
                        loadComponent: () =>
                            import('./guides/guide-form/guide-form').then((m) => m.GuideForm),
                        data: { breadcrumb: 'Create' },
                    },
                    {
                        path: ':uuid',
                        children: [
                            {
                                path: '',
                                loadComponent: () =>
                                    import('./guides/guide-detail/guide-detail').then(
                                        (m) => m.GuideDetail,
                                    ),
                                data: { breadcrumb: 'Details' },
                            },
                            {
                                path: 'edit',
                                loadComponent: () =>
                                    import('./guides/guide-form/guide-form').then(
                                        (m) => m.GuideForm,
                                    ),
                                data: { breadcrumb: 'Edit' },
                            },
                        ],
                    },
                ],
            },
            {
                path: 'users',
                data: { breadcrumb: 'Users' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./users/user-list/user-list').then((m) => m.UserList),
                    },
                    {
                        path: ':uuid',
                        loadComponent: () =>
                            import('./users/user-detail/user-detail').then((m) => m.UserDetail),
                        data: { breadcrumb: 'Details' },
                    },
                ],
            },
            {
                path: 'cities',
                data: { breadcrumb: 'Cities' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./cities/city-list/city-list').then((m) => m.CityList),
                    },
                    {
                        path: 'create',
                        loadComponent: () =>
                            import('./cities/city-form/city-form').then((m) => m.CityForm),
                        data: { breadcrumb: 'Create' },
                    },
                    {
                        path: ':uuid',
                        children: [
                            {
                                path: '',
                                loadComponent: () =>
                                    import('./cities/city-detail/city-detail').then(
                                        (m) => m.CityDetail,
                                    ),
                                data: { breadcrumb: 'Details' },
                            },
                            {
                                path: 'edit',
                                loadComponent: () =>
                                    import('./cities/city-form/city-form').then((m) => m.CityForm),
                                data: { breadcrumb: 'Edit' },
                            },
                        ],
                    },
                ],
            },
            {
                path: 'languages',
                data: { breadcrumb: 'Languages' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./languages/language-list/language-list').then(
                                (m) => m.LanguageList,
                            ),
                    },
                ],
            },
            {
                path: 'roles',
                data: { breadcrumb: 'Roles & Permissions' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./roles-permissions/roles-permissions').then(
                                (m) => m.RolesPermissions,
                            ),
                    },
                ],
            },
            {
                path: 'articles',
                data: { breadcrumb: 'Articles' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./articles/article-list/article-list').then(
                                (m) => m.ArticleList,
                            ),
                    },
                    {
                        path: ':uuid',
                        loadComponent: () =>
                            import('./articles/article-detail/article-detail').then(
                                (m) => m.ArticleDetail,
                            ),
                        data: { breadcrumb: 'Details' },
                    },
                ],
            },
            {
                path: 'reviews',
                data: { breadcrumb: 'Reviews' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./reviews/review-list/review-list').then((m) => m.ReviewList),
                    },
                ],
            },
            {
                path: 'reservations',
                data: { breadcrumb: 'Reservations' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./reservations/reservation-list/reservation-list').then(
                                (m) => m.ReservationList,
                            ),
                    },
                    {
                        path: ':uuid',
                        loadComponent: () =>
                            import('./reservations/reservation-detail/reservation-detail').then(
                                (m) => m.ReservationDetail,
                            ),
                        data: { breadcrumb: 'Details' },
                    },
                ],
            },
            {
                path: 'payments',
                data: { breadcrumb: 'Payments' },
                children: [
                    {
                        path: '',
                        loadComponent: () =>
                            import('./payments/payment-list/payment-list').then(
                                (m) => m.PaymentList,
                            ),
                    },
                ],
            },
            {
                path: 'profile',
                loadComponent: () =>
                    import('../../shared/pages/profile/profile').then((m) => m.Profile),
                data: { breadcrumb: 'Profile' },
            },
        ],
    },
];
