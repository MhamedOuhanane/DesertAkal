import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
    {
        path: 'tours/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'guides/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/tours/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/tours/:uuid/edit',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/guides/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/guides/:uuid/edit',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/users/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/cities/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/cities/:uuid/edit',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/articles/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'dashboard/reservations/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'guide/dashboard/assignments/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'tourist/dashboard/bookings/:uuid',
        renderMode: RenderMode.Client,
    },
    {
        path: 'tourist/dashboard/bookings/:uuid/pay',
        renderMode: RenderMode.Client,
    },
    {
        path: 'tourist/dashboard/bookings/:uuid/confirmation',
        renderMode: RenderMode.Client,
    },
    {
        path: 'tourist/dashboard/posts/:uuid/edit',
        renderMode: RenderMode.Client,
    },
    {
        path: 'reservations/verify/:uuid',
        renderMode: RenderMode.Client,
    },

    {
        path: '**',
        renderMode: RenderMode.Prerender,
    },
];
