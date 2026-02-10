import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStore } from '../store/auth.store';
import { DeviceService } from '../services/device-service';
import { catchError, throwError } from 'rxjs';
import { toast } from 'ngx-sonner';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
    const authStore = inject(AuthStore);
    const deviceService = inject(DeviceService);

    const token = authStore.token();
    const isRefreshPath = req.url.includes('/auth/refresh');
    const isLogoutPath = req.url.includes('/auth/logout');

    let headers = req.headers;

    if (isRefreshPath) {
        headers = headers.set('X-Device-ID', deviceService.getDeviceId());
    } else if (!token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
    }

    const authReq = req.clone({
        headers,
        withCredentials: isRefreshPath || isLogoutPath,
    });

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 && !isRefreshPath) {
                toast.error('Session Expired', {
                    description:
                        'For your security, inactive sessions are closed after 30 days. Please log in again to continue.',
                    duration: 5000,
                });
            }

            return throwError(() => error);
        }),
    );
};
