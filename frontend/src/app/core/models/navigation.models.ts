import { UserRole } from './user.models';

export interface MenuItem {
    label: string;
    icon: string;
    path: string;
    roles: UserRole[];
}

export interface MenuGroup {
    title: string;
    roles: UserRole[];
    items: MenuItem[];
}

export interface PublicNavLink {
    label: string;
    path: string;
    icon: string;
    roles: UserRole[];
    requiresAuth: boolean;
}
