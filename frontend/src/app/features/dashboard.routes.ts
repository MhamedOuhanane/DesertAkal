import { Routes } from '@angular/router';
import { DashboardLayout } from '../layouts/dashboard-layout/dashboard-layout';
import { roleGuard } from '../core/guards/role-guard';

export const DASHBOARD_ROUTES: Routes = [
    {
        path: '',
        component: DashboardLayout,
        data: { breadcrumb: 'Dashboard' },
        children: [
            {
                path: 'dashboard',
                canActivate: [roleGuard],
                data: { roles: ['ADMIN'] },
                loadChildren: () => import('./admin/admin.routes').then((r) => r.ADMIN_ROUTES),
            },
            {
                path: 'guide',
                canActivate: [roleGuard],
                data: { roles: ['GUIDE'] },
                loadChildren: () => import('./guide/guide.routes').then((r) => r.GUIDE_ROUTES),
            },

            {
                path: 'tourist',
                canActivate: [roleGuard],
                data: { roles: ['TOURIST'] },
                loadChildren: () =>
                    import('./tourist/tourist.routes').then((r) => r.TOURIST_ROUTES),
            },
        ],
    },
];
