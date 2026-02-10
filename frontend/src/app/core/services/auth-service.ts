import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { DeviceService } from './device-service';
import { environment } from '../../../environments/environment.development';
import { ActiveSession, LoginRequest, Register } from '../auth/auth.models';
import { Observable } from 'rxjs';
import { UserAuth } from '../models/user.models';
import { ApiResponse } from '../models/response.models';

@Injectable({
    providedIn: 'root',
})
export class AuthService {
    private http = inject(HttpClient);
    private deviceService = inject(DeviceService);
    private apiUrl = `${environment.apiUrl}/auth`;

    register(credentials: Register): Observable<ApiResponse<null>> {
        return this.http.post<ApiResponse<null>>(`${this.apiUrl}/register`, credentials);
    }

    login(credentials: Omit<LoginRequest, 'deviceId'>): Observable<ApiResponse<UserAuth>> {
        const loginBody: LoginRequest = {
            ...credentials,
            deviceId: this.deviceService.getDeviceId(),
        };
        return this.http.post<ApiResponse<UserAuth>>(`${this.apiUrl}/login`, loginBody);
    }

    refresh(): Observable<ApiResponse<UserAuth>> {
        return this.http.post<ApiResponse<UserAuth>>(`${this.apiUrl}/refresh`, {});
    }

    resendVerificationEmail(email: string): Observable<ApiResponse<null>> {
        return this.http.post<ApiResponse<null>>(`${this.apiUrl}/verify-email`, { email: email });
    }

    confirmEmail(token: string): Observable<ApiResponse<null>> {
        return this.http.get<ApiResponse<null>>(`${this.apiUrl}/verify-email`, {
            params: { token },
        });
    }

    getMySessions(): Observable<ApiResponse<ActiveSession[]>> {
        return this.http.get<ApiResponse<ActiveSession[]>>(`${this.apiUrl}/sessions`);
    }

    remoteLogout(sessionUuid: string): Observable<ApiResponse<null>> {
        return this.http.post<ApiResponse<null>>(`${this.apiUrl}/remote-logout`, {
            sessionUuid,
        });
    }

    logout(): Observable<ApiResponse<null>> {
        return this.http.post<ApiResponse<null>>(`${this.apiUrl}/logout`, {});
    }
}
