import { RoleEnum } from "../enums/role.enum";

export interface MenuItem {
    label: string;
    icon: string;
    path: string;
    roles: RoleEnum[];
    exact?: boolean;
}

export interface MenuGroup {
    title: string;
    roles: RoleEnum[];
    items: MenuItem[];
}

export interface PublicNavLink {
    label: string;
    path: string;
    icon: string;
    roles: RoleEnum[];
    requiresAuth: boolean;
    exact?: boolean;
}
