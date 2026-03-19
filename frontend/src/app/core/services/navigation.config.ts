import { MenuGroup, MenuItem, PublicNavLink } from '../models/navigation.models';

export const NAV_CONFIG = {
    PUBLIC_LINKS: [
        {
            label: 'Home',
            path: '/',
            icon: 'home',
            roles: ['VISITOR', 'TOURIST', 'GUIDE', 'ADMIN'],
            requiresAuth: false,
            exact: true,
        },
        {
            label: 'Tours',
            path: '/tours',
            icon: 'explore',
            roles: ['VISITOR', 'TOURIST', 'GUIDE', 'ADMIN'],
            requiresAuth: false,
        },
        {
            label: 'About',
            path: '/about',
            icon: 'info',
            roles: ['VISITOR', 'TOURIST', 'GUIDE', 'ADMIN'],
            requiresAuth: false,
        },
        {
            label: 'Guides',
            path: '/guides',
            icon: 'badge',
            roles: ['TOURIST', 'GUIDE', 'ADMIN'],
            requiresAuth: true,
        },
        {
            label: 'Blog',
            path: '/blog',
            icon: 'article',
            roles: ['TOURIST', 'GUIDE', 'ADMIN'],
            requiresAuth: true,
        },
        {
            label: 'Contact',
            path: '/contact',
            icon: 'mail',
            roles: ['VISITOR', 'TOURIST', 'GUIDE', 'ADMIN'],
            requiresAuth: false,
        },
    ] as PublicNavLink[],

    USER_MENU_LINKS: [
        {
            label: 'My Profile',
            icon: 'person',
            path: '/tourist/dashboard/profile',
            roles: ['TOURIST'],
        },
        {
            label: 'My Profile',
            icon: 'person',
            path: '/guide/dashboard/profile',
            roles: ['GUIDE'],
        },
        {
            label: 'My Bookings',
            icon: 'book_online',
            path: '/tourist/dashboard/bookings',
            roles: ['TOURIST'],
        },
        {
            label: 'My Articles',
            icon: 'edit_note',
            path: '/tourist/dashboard/posts',
            roles: ['TOURIST'],
        },
        {
            label: 'My Reviews',
            icon: 'star_rate',
            path: '/tourist/dashboard/reviews',
            roles: ['TOURIST'],
        },
        {
            label: 'My Assignments',
            icon: 'assignment',
            path: '/guide/dashboard/assignments',
            roles: ['GUIDE'],
        },
        {
            label: 'My Earnings',
            icon: 'payments',
            path: '/guide/dashboard/earnings',
            roles: ['GUIDE'],
        },

        // { label: 'Settings', icon: 'settings', path: '/dashboard/settings', roles: ['TOURIST', 'GUIDE'] },
    ] as MenuItem[],

    SIDEBAR_GROUPS: [
        {
            title: 'Overview',
            roles: ['ADMIN', 'GUIDE', 'TOURIST'],
            items: [
                {
                    label: 'Admin Dashboard',
                    icon: 'dashboard',
                    path: '/dashboard',
                    roles: ['ADMIN'],
                    exact: true,
                },
                {
                    label: 'Guide Dashboard',
                    icon: 'dashboard',
                    path: '/guide/dashboard',
                    roles: ['GUIDE'],
                    exact: true,
                },
                {
                    label: 'Tourist Dashboard',
                    icon: 'dashboard',
                    path: '/tourist/dashboard',
                    roles: ['TOURIST'],
                    exact: true,
                },
            ],
        },
        {
            title: 'Management',
            roles: ['ADMIN'],
            items: [
                {
                    label: 'All Users',
                    icon: 'group',
                    path: '/dashboard/users',
                    roles: ['ADMIN'],
                    badge: '24',
                },
                {
                    label: 'Guides',
                    path: '/dashboard/guides',
                    icon: 'directions_walk',
                    roles: ['ADMIN'],
                },
                {
                    label: 'Cities',
                    path: '/dashboard/cities',
                    icon: 'location_city',
                    roles: ['ADMIN'],
                },
                {
                    label: 'Languages',
                    path: '/dashboard/languages',
                    icon: 'translate',
                    roles: ['ADMIN'],
                },
                {
                    label: 'Roles & Permissions',
                    path: '/dashboard/roles',
                    icon: 'admin_panel_settings',
                    roles: ['ADMIN'],
                },
                {
                    label: 'Tours Management',
                    icon: 'explore',
                    path: '/dashboard/tours',
                    roles: ['ADMIN'],
                },
                {
                    label: 'Reservations',
                    icon: 'event_available',
                    path: '/dashboard/reservations',
                    roles: ['ADMIN'],
                    badge: 'New',
                },
                {
                    label: 'Article Validation',
                    icon: 'article',
                    path: '/dashboard/articles',
                    roles: ['ADMIN'],
                },
                {
                    label: 'Reviews',
                    path: '/dashboard/reviews',
                    icon: 'rate_review',
                    roles: ['ADMIN'],
                },
            ],
        },
        {
            title: 'My Travel Space',
            roles: ['TOURIST'],
            items: [
                {
                    label: 'My Bookings',
                    icon: 'book_online',
                    path: '/tourist/dashboard/bookings',
                    roles: ['TOURIST'],
                },
                // {
                //     label: 'Interactive Map',
                //     icon: 'map',
                //     path: '/tourist/dashboard/map',
                //     roles: ['TOURIST'],
                // },
                {
                    label: 'My Publications',
                    icon: 'history_edu',
                    path: '/tourist/dashboard/posts',
                    roles: ['TOURIST'],
                },
                {
                    label: 'My Reviews',
                    icon: 'star_rate',
                    path: '/tourist/dashboard/reviews',
                    roles: ['TOURIST'],
                },
            ],
        },
        {
            title: 'My Work',
            roles: ['GUIDE'],
            items: [
                {
                    label: 'Assigned Tours',
                    icon: 'tour',
                    path: '/guide/dashboard/tours',
                    roles: ['GUIDE'],
                },
                {
                    label: 'My Schedule',
                    icon: 'event_available',
                    path: '/guide/dashboard/schedule',
                    roles: ['GUIDE'],
                },
            ],
        },
        {
            title: 'Finance',
            roles: ['ADMIN', 'GUIDE', 'TOURIST'],
            items: [
                {
                    label: 'System Payments',
                    icon: 'payments',
                    path: '/dashboard/payments',
                    roles: ['ADMIN'],
                },
                {
                    label: 'My Earnings',
                    icon: 'account_balance_wallet',
                    path: '/guide/dashboard/earnings',
                    roles: ['GUIDE'],
                },
                {
                    label: 'My Payments',
                    icon: 'receipt',
                    path: '/tourist/dashboard/payments',
                    roles: ['TOURIST'],
                },
            ],
        },
        {
            title: 'Account',
            roles: ['ADMIN', 'GUIDE', 'TOURIST'],
            items: [
                {
                    label: 'My Profile',
                    icon: 'person',
                    path: '/dashboard/profile',
                    roles: ['ADMIN'],
                },
                {
                    label: 'My Profile',
                    icon: 'person',
                    path: '/guide/dashboard/profile',
                    roles: ['GUIDE'],
                },
                {
                    label: 'My Profile',
                    icon: 'person',
                    path: '/tourist/dashboard/profile',
                    roles: ['TOURIST'],
                },
            ],
        },
    ] as MenuGroup[],
} as const;
