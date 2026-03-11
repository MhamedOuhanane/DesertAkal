import { RoleEnum } from '../enums/role.enum';

export interface LoginRequest {
    username: string;
    password: string;
    deviceId?: string;
    provider?: string;
    providerId?: string;
}

export interface LoginResponse {
    readonly uuid: string;
    readonly username: string;
    readonly fullName: string;
    readonly photo: string;
    readonly role: RoleEnum;
    readonly accessToken: string;
    readonly refreshToken?: string | null;
}

export interface Register {
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
    roleUuid: string;
    nationality?: string;
    language?: string;
    oauthProvider?: string;
    providerId?: string;
}

export interface EmailVerification {
    email: string;
}
