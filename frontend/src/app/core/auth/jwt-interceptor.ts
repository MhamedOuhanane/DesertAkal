import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStore } from './auth.store';
import { DeviceService } from '../services/device-service';
import { catchError, switchMap, throwError } from 'rxjs';
import { toast } from 'ngx-sonner';
import { AuthService } from './auth-service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
    const authStore = inject(AuthStore);
    const deviceService = inject(DeviceService);
    const authService = inject(AuthService);

    const token = authStore.token();
    const isRefreshPath = req.url.includes('/auth/refresh');
    const isAuthPath = req.url.includes('/auth');
    const isLogoutPath = req.url.includes('/auth/logout');
    const isLoginPath = req.url.includes('/auth/login');

    let headers = req.headers;

    if (isRefreshPath) {
        headers = headers.set('X-Device-ID', deviceService.getDeviceId());
    } else if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
    }

    const authReq = req.clone({
        headers,
        withCredentials: isLoginPath || isLogoutPath || isRefreshPath,
    });

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
            const isAuthEndpoint = !isAuthPath || !isRefreshPath;
            if (error.status === 401 && !isAuthEndpoint) {
                return authService.refresh().pipe(
                    switchMap((response) => {
                        if (response.status === 200 && response.data) {
                            authStore.setRefreshToken(response.data.accessToken);

                            const retryReq = req.clone({
                                headers: req.headers.set(
                                    'Authorization',
                                    `Bearer ${response.data.accessToken}`,
                                ),
                                withCredentials: true,
                            });
                            return next(retryReq);
                        }

                        return throwError(() => error);
                    }),
                    catchError((refreshError) => {
                        authStore.logout();
                        toast.error('Session Expired', {
                            description: 'Please log in again to continue.',
                        });
                        return throwError(() => refreshError);
                    }),
                );
            }

            return throwError(() => error);
        }),
    );
};
