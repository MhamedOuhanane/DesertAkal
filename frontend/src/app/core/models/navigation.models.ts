import { userRole } from "./user.models";

export interface MenuItem  {
    label: string;
    icon: string;
    path: string;
    roles: userRole[];
}

export interface MenuGroup {
    title: string;
    roles: userRole[];
    items: MenuItem[];
}

export interface PublicNavLink {
    label: string;
    path: string;
    icon: string;
    roles: userRole[];
    requiresAuth: boolean;
}