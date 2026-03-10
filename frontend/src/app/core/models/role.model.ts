export interface Role {
    readonly uuid: string;
    readonly name: string;
}

export interface RoleCreate {
    name: string;
    permissionUuids: string[];
}

export interface RoleFind extends Role {
    readonly permissionUuids: string[];
}

export interface RoleUpdate {
    name?: string;
    permissionUuids?: string[];
}
