export interface LoginRequest {
    username: string;
    password: string;
    deviceId: string | null;
}

export interface Register {
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
    roleUuid: string | null;
    nationality: string | null;
    language: string | null;
}

export interface ActiveSession {
    sessionUuid: string;
    ipAddress: string;
    userAgent: string;
    lastActive: string;
    expiresAt: string;
}
