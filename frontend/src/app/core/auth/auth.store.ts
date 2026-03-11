import {
    patchState,
    signalStore,
    withComputed,
    withHooks,
    withMethods,
    withState,
} from '@ngrx/signals';
import { UserAuth } from '../models/user.models';
import { computed, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../../../environments/environment.development';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth-service';
import { Router } from '@angular/router';
import { RoleEnum } from '../enums/role.enum';
import { LoginRequest, LoginResponse } from './auth.models';
import { ApiResponse } from '../models/response.models';

export interface AuthState {
    user: UserAuth | null;
    token: string | null;
    loading: boolean;
}

const initialState: AuthState = {
    user: null,
    token: null,
    loading: false,
};

export const AuthStore = signalStore(
    { providedIn: 'root' },

    withState(initialState),

    withComputed(({ token, user }) => ({
        isAuthenticated: computed(() => !!token()),
        userRole: computed(() => user()?.role || RoleEnum.VISITOR),
        userPhoto: computed(() => user()?.photo || 'assets/defaults/default-profile.png'),
    })),

    withMethods(
        (
            store,
            platformId = inject(PLATFORM_ID),
            cookieService = inject(CookieService),
            authService = inject(AuthService),
            router = inject(Router),
        ) => ({
            async login(credentials: LoginRequest): Promise<boolean> {
                if (!isPlatformBrowser(platformId)) return false;

                patchState(store, { loading: true });

                try {
                    const response: ApiResponse<LoginResponse> = await firstValueFrom(
                        authService.login(credentials),
                    );

                    if (response.status === 200 && response.data) {
                        const userData: UserAuth = {
                            uuid: response.data.uuid,
                            username: response.data.username,
                            fullName: response.data.fullName,
                            photo: response.data.photo,
                            role: response.data.role,
                        };

                        this.setLogin(response.data.accessToken, userData);

                        toast.success('Welcome back to DesertAkal!');

                        patchState(store, { loading: false });

                        return true;
                    }

                    patchState(store, { loading: false });
                    return false;
                } catch (error: any) {
                    const msg =
                        error?.error?.message || 'Login failed. Please check your credentials.';
                    toast.error(msg);

                    patchState(store, { loading: false });

                    return false;
                }
            },

            setLogin(token: string, user: UserAuth) {
                if (!isPlatformBrowser(platformId)) return;

                const isSecure = environment.secureCookie;

                const tokenExpires = new Date();
                tokenExpires.setMinutes(tokenExpires.getMinutes() + 15);
                cookieService.set('auth_token', token, tokenExpires, '/', '', isSecure, 'Strict');

                cookieService.set(
                    'user_data',
                    JSON.stringify(user),
                    30,
                    '/',
                    '',
                    isSecure,
                    'Strict',
                );

                patchState(store, { token, user });
            },

            setRefreshToken(newToken: string) {
                if (!isPlatformBrowser(platformId)) return;

                const isSecure = environment.secureCookie;
                // const expires = new Date();
                // expires.setMinutes(expires.getMinutes() + 15);
                const longTerm = 30;

                cookieService.set('auth_token', newToken, longTerm, '/', '', isSecure, 'Strict');

                const currentUser = store.user();
                if (currentUser) {
                    cookieService.set(
                        'user_data',
                        JSON.stringify(currentUser),
                        longTerm,
                        '/',
                        '',
                        isSecure,
                        'Strict',
                    );
                }

                patchState(store, { token: newToken });
            },

            handleOAuthSuccess(data: any) {
                patchState(store, { loading: true });

                const isSecure = environment.secureCookie;
                // const expires = new Date();
                // expires.setMinutes(expires.getMinutes() + 15);
                const longTerm = 30;

                cookieService.set('auth_token', data.token, longTerm, '/', '', isSecure, 'Strict');
                const user: UserAuth = {
                    uuid: data.userUuid,
                    username: data.username,
                    fullName: data.fullName,
                    role: data.role,
                    photo: data.photo,
                };

                cookieService.set(
                    'user_data',
                    JSON.stringify(user),
                    longTerm,
                    '/',
                    '',
                    isSecure,
                    'Strict',
                );

                patchState(store, {
                    user: user,
                    token: data.token,
                    loading: false,
                });
            },

            async logout() {
                if (!isPlatformBrowser(platformId)) return;

                patchState(store, { loading: true });

                try {
                    const response = await firstValueFrom(authService.logout());
                    toast.success(response.message);

                    cookieService.delete('auth_token', '/');
                    cookieService.delete('user_data', '/');
                    patchState(store, initialState);
                    await router.navigate(['/auth/login']);
                } catch (error) {
                    toast.error('Server logout failed, cleaning local storage anyway.');
                }
            },

            clearAuth() {
                if (!isPlatformBrowser(platformId)) return;

                cookieService.delete('auth_token', '/');
                cookieService.delete('user_data', '/');

                patchState(store, initialState);

                router.navigate(['/auth/login']);
            },

            setLoading(value: boolean) {
                patchState(store, { loading: value });
            },
        }),
    ),

    withHooks({
        async onInit(
            store,
            platformId = inject(PLATFORM_ID),
            cookieService = inject(CookieService),
            authService = inject(AuthService),
        ) {
            if (!isPlatformBrowser(platformId)) return;

            const savedToken = cookieService.get('auth_token');
            const savedUserJson = cookieService.get('user_data');

            if (savedToken && savedUserJson) {
                try {
                    const user = JSON.parse(savedUserJson) as UserAuth;
                    patchState(store, { token: savedToken, user });
                } catch {
                    store.clearAuth();
                }
            } else if (!savedToken && savedUserJson) {
                try {
                    patchState(store, { loading: true });
                    
                    const response = await firstValueFrom(authService.refresh());
                    console.log(response);
                    
                    
                    if (response.status === 200 && response.data) {
                        const user = JSON.parse(savedUserJson) as UserAuth;
                        store.setRefreshToken(response.data.accessToken);
                        patchState(store, { user, loading: false });
                    }
                } catch (error: any) {
                    const msg = error?.error?.message || 'Session expired, please login again';
                    toast.error(msg);
                    if (error.status === 401 || error.status === 403) {
                        store.clearAuth();
                    }
                    patchState(store, { loading: false });
                }
            }
        },
    }),
);
