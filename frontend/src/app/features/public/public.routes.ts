import { Routes } from '@angular/router';
import { authGuard } from '../../core/auth/auth-guard';
import { roleGuard } from '../../core/guards/role-guard';

export const PUBLIC_ROUTES: Routes = [
    { path: '', loadComponent: () => import('./home/home').then((m) => m.Home) },
    {
        path: 'tours',
        children: [
            {
                path: '',
                loadComponent: () =>
                    import('./tours/tour-list-public/tour-list-public').then(
                        (m) => m.TourListPublic,
                    ),
            },
            {
                path: ':uuid',
                loadComponent: () =>
                    import('./tours/tour-detail-public/tour-detail-public').then(
                        (m) => m.TourDetailPublic,
                    ),
            },
        ],
    },
    {
        path: 'guides',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['TOURIST']},
        children: [
            {
                path: '',
                loadComponent: () =>
                    import('./guides/guide-list-public/guide-list-public').then(
                        (m) => m.GuideListPublic,
                    ),
            },
            {
                path: ':uuid',
                loadComponent: () =>
                    import('./guides/guide-detail-public/guide-detail-public').then(
                        (m) => m.GuideDetailPublic,
                    ),
            },
        ],
    },
    { 
        path: 'blog',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['TOURIST']},
        loadComponent: () => import('./blog/blog').then((m) => m.Blog) 
    },
    { path: 'about', loadComponent: () => import('./about/about').then((m) => m.About) },
    { path: 'contact', loadComponent: () => import('./contact/contact').then((m) => m.Contact) },
];
