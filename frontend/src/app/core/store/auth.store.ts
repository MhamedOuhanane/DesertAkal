import {
    patchState,
    signalStore,
    withComputed,
    withHooks,
    withMethods,
    withState,
} from '@ngrx/signals';
import { UserAuth } from '../models/user.model';
import { computed, inject } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../../../environments/environment.development';

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
        userRole: computed(() => user()?.role || 'VISITOR'),
        userPhoto: computed(() => user()?.photo || 'assets/defaults/default-profile.png'),
    })),

    withMethods((store, cookieService = inject(CookieService)) => ({
        setLogin(token: string, user: UserAuth) {
            patchState(store, { loading: true });

            const expires = new Date();
            expires.setMinutes(expires.getMinutes() + 15);

            const isSecure = environment.secureCookie;

            cookieService.set('auth_token', token, expires, '/', '', isSecure, 'Strict');
            cookieService.set('user_data', JSON.stringify(user), 30, '/', '', isSecure, 'Strict');

            patchState(store, {
                token,
                user,
                loading: false,
            });
        },

        setRefreshToken(newToken: string) {
            const isSecure = environment.secureCookie;

            const expires = new Date();
            expires.setMinutes(expires.getMinutes() + 15);

            cookieService.set('auth_token', newToken, expires, '/', '', isSecure, 'Strict');

            const currentUser = store.user();
            if (currentUser) {
                cookieService.set(
                    'user_data',
                    JSON.stringify(currentUser),
                    30,
                    '/',
                    '',
                    isSecure,
                    'Strict',
                );
            }

            patchState(store, { token: newToken });
        },

        logout() {
            cookieService.delete('auth_token', '/');
            cookieService.delete('user_data', '/');
            patchState(store, initialState);
        },

        setLoading(value: boolean) {
            patchState(store, { loading: value });
        },
    })),

    withHooks({
        onInit(store, cookieService = inject(CookieService)) {
            const savedToken = cookieService.get('auth_token');
            const savedUserJson = cookieService.get('user_data');

            if (savedToken && savedUserJson) {
                try {
                    const user = JSON.parse(savedUserJson) as UserAuth;

                    patchState(store, { token: savedToken, user });
                } catch (error) {
                    store.logout();
                }
            }
        },
    }),
);
