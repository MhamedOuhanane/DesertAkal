export interface RefreshToken {
    readonly uuid: string;
    readonly token: String;
    readonly userUuid: string;
    readonly familyId: string;
    readonly deviceId: String;
    readonly userAgent: String;
    readonly ipAddress: String;
    readonly createdAt: string | Date;
    readonly expiresAt: string | Date;
    readonly revoked: boolean;
    readonly used: boolean;
}

export interface RefreshTokenFull extends RefreshToken {
    readonly parentToken: String;
    readonly usedAt: string | Date;
    readonly revokedAt: string | Date;
    readonly reuseDetected: boolean;
}

export interface ActiveSession {
    readonly sessionUuid: string;
    readonly ipAddress: string;
    readonly userAgent: string;
    readonly lastActive: string | Date;
    readonly expiresAt: string | Date;
}

export interface RemoteLogoutRequest {
    readonly sessionUuid: string;
    readonly password: string;
}
