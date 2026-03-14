import { PageAble } from "./response.models";

export interface Permission {
    uuid: string;
    name: string;
}

export interface PermissionFilters extends PageAble {
    search?: string;
}

export interface PermissionRequest {
    name: string;
}

export interface PermissionUpdate {
    name: string;
}
