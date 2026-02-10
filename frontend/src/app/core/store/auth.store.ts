import { patchState, signalStore, withComputed, withHooks, withMethods, withState } from '@ngrx/signals';
import { UserAuth } from "../models/user.models";
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
}

export const AuthState = signalStore(
    { providedIn: 'root' },

    withState(initialState),

    withComputed(({token, user}) => ({
        isAuthenticated: computed(() => !token()),
        userRole: computed(() => user()?.role || 'VISITOR'),
        userPhoto: computed(() => user()?.photo || 'assets/defaults/default-profile.png'),
    })),

    withMethods((store, cookieService =inject(CookieService)) => ({
        setLogin(token: string, user: UserAuth) {
            patchState(store, { loading: true });

            const expires = new Date();
            expires.setMinutes(expires.getMinutes() + 15);

            const isSecure = environment.secureCookie;
            
            cookieService.set('auth_token', token, expires, '/', '', isSecure, 'Lax');
            cookieService.set('user_data', JSON.stringify(user), 30, '/', '', isSecure, 'Lax');

            patchState(store, {
                token, 
                user, 
                loading: false,
            })
        },

        logout() {
            cookieService.delete('auth_token', '/');
            cookieService.delete('user_data', '/');
            patchState(store, initialState);
        },

        setLoading(value: boolean) {
            patchState(store, { loading: value });
        }
    })),

    withHooks({
        onInit(store, cookieService = inject(CookieService)) {
            const savedToken = cookieService.get('auth_token');
            const savedUserJson = cookieService.get('user_data');

            if(savedToken && savedUserJson) {
                try {
                    const user = JSON.parse(savedToken) as UserAuth;

                    patchState(store, { token: savedToken, user});
                } catch (error) {
                    store.logout();
                }
            }
        }
    })
);