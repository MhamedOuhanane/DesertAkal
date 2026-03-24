import { HttpBackend, HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Register } from '../auth/auth.models';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/response.models';
import { DeviceService } from '../services/device-service';
import { ActiveSession } from '../models/refresh-token.model';

@Injectable({
    providedIn: 'root',
})
export class AuthService {
    private http = inject(HttpClient);
    private httpBackend = inject(HttpBackend);
    private deviceService = inject(DeviceService);
    private apiUrl = `${environment.apiUrl}/auth`;

    private httpWithoutInterceptors = new HttpClient(this.httpBackend);

    register(credentials: Register): Observable<ApiResponse<null>> {
        return this.http.post<ApiResponse<null>>(`${this.apiUrl}/register`, credentials);
    }

    login(credentials: Omit<LoginRequest, 'deviceId'>): Observable<ApiResponse<LoginResponse>> {
        const loginBody: LoginRequest = {
            ...credentials,
            deviceId: this.deviceService.getDeviceId(),
        };
        return this.http.post<ApiResponse<LoginResponse>>(`${this.apiUrl}/login`, loginBody);
    }

    refresh(): Observable<ApiResponse<LoginResponse>> {
        return this.httpWithoutInterceptors.post<ApiResponse<LoginResponse>>(
            `${this.apiUrl}/refresh`,
            {},
            {
                headers: { 'X-Device-ID': this.deviceService.getDeviceId() },
                withCredentials: true,
            },
        );
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
