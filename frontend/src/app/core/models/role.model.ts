import { Permission } from './permission.model';
import { PageAble } from './response.models';

export interface Role {
    readonly uuid: string;
    readonly name: string;
}

export interface RoleFilters extends PageAble {
    search?: string;
}

export interface RoleCreate {
    name: string;
    permissionUuids: string[];
}

export interface RoleFind extends Role {
    readonly permissions: Permission[];
}

export interface RoleUpdate {
    name?: string;
    permissionUuids?: string[];
}
