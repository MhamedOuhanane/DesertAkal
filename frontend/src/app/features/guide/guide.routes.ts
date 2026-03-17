import { Routes } from '@angular/router';
import { DashboardLayout } from '../../layouts/dashboard-layout/dashboard-layout';

export const GUIDE_ROUTES: Routes = [
    {
        path: '',
        component: DashboardLayout,
        children: [],
    },
];
