import { RoleEnum } from "../enums/role.enum";
import { UserStatus } from "../enums/user-status.enum";

export type UserRole = RoleEnum;

export interface UserAuth {
    uuid: string;
    username: string;
    fullName: string;
    photo?: string;
    role: RoleEnum;
}

export interface User {
    readonly uuid: string;
    readonly firstName: string;
    readonly lastName: string;
    readonly photo: string;
    readonly username: string;
    readonly email: string;
    readonly role: RoleEnum;
}

export interface UserFind extends User {
    readonly phone: string;
    readonly status: UserStatus;
    readonly lastLoginAt: string | Date;
    readonly createdAt: string | Date;
    readonly updatedAt: string | Date;
    readonly oauthProviders: string[];
}

export interface UserUpdate {
    firstName?: string;
    lastName?: string;
    phone?: string;
    status?: string;
}

