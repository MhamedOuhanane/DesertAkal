import { Routes } from "@angular/router";
import { DashboardLayout } from "../../layout/dashboard-layout/dashboard-layout";

export const ADMIN_ROUTES: Routes = [
    {
        path: '',
        component: DashboardLayout,
        children: [
            {
                path: 'dashboard',
                loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard)
            },
            {
                path: 'tours',
                loadComponent: () => import('./tours-management/tours-management').then(m => m.ToursManagement)
            }
        ]
    }
]