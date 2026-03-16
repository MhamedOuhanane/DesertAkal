import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStore } from './auth.store';
import { DeviceService } from '../services/device-service';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take, Observable } from 'rxjs';
import { toast } from 'ngx-sonner';
import { AuthService } from './auth-service';

let isRefreshing = false;
const refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

export const jwtInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
    const authStore = inject(AuthStore);
    const deviceService = inject(DeviceService);
    const authService = inject(AuthService);

    const token = authStore.token();
    const { url } = req;
    const isAuthPath = url.includes('/api/auth');
    const isRefreshPath = url.includes('/api/auth/refresh');

    let { headers } = req;
    
    if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
    }
    
    if (isRefreshPath) {
        headers = headers.set('X-Device-ID', deviceService.getDeviceId());
    }

    const authReq = req.clone({
        headers,
        withCredentials: true 
    });

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse): Observable<HttpEvent<unknown>> => {
            if (error.status === 401 && !isAuthPath) {
                return handle401Error(authService, authStore, authReq, next);
            }
            return throwError(() => error);
        })
    );
};

function handle401Error(
    authService: AuthService, 
    authStore: any,
    request: HttpRequest<unknown>, 
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> {
    if (!isRefreshing) {
        isRefreshing = true;
        refreshTokenSubject.next(null);

        return authService.refresh().pipe(
            switchMap((response: any) => {
                isRefreshing = false;
                const newToken = response.data.accessToken;
                
                authStore.setRefreshToken(newToken); 
                refreshTokenSubject.next(newToken);
                
                return next(request.clone({
                    headers: request.headers.set('Authorization', `Bearer ${newToken}`)
                }));
            }),
            catchError((err) => {
                isRefreshing = false;
                authStore.logout();
                toast.error('Session Expired', {
                    description: 'Please log in again to continue.',
                });
                return throwError(() => err);
            })
        );
    } else {
        return refreshTokenSubject.pipe(
            filter(token => token !== null),
            take(1),
            switchMap((token) => next(request.clone({
                headers: request.headers.set('Authorization', `Bearer ${token}`)
            })))
        );
    }
}