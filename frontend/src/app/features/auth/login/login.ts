import { Component, effect, inject, PLATFORM_ID, signal, untracked } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LoginRequest } from '../../../core/auth/auth.models';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';
import { ScreenService } from '../../../core/services/screen-service';
import { toast } from 'ngx-sonner';
import { AuthStore } from '../../../core/auth/auth.store';
import { OauthLogin } from '../../../shared/components/oauth-login/oauth-login';
import { TextInput } from '../../../shared/components/text-input/text-input';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, BrandLogo, OauthLogin, TextInput],
    templateUrl: './login.html',
})
export class Login {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private fb = inject(FormBuilder);
    protected readonly authStore = inject(AuthStore);
    protected screenService = inject(ScreenService);

    constructor() {
        effect(() => {
            this.route.queryParams.subscribe((params) => {
                const error = params['error'];
                if (error) {
                    toast.error(error);
                }
            });
        });
    }

    isLoading = this.authStore.loading;

    showPassword = signal(false);

    loginForm = this.fb.nonNullable.group({
        username: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]],
    });

    togglePassword(): void {
        this.showPassword.update((v) => !v);
    }

    async onSubmit(): Promise<void> {
        if (this.loginForm.invalid) {
            this.loginForm.markAllAsTouched();
            return;
        }

        const credentials: LoginRequest = this.loginForm.getRawValue();
        const success = await this.authStore.login(credentials);

        if (success) {
            this.loginForm.get('password')?.reset();
            const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
            await this.router.navigateByUrl(returnUrl);
        }
    }

    loginWithGoogle(): void {
        toast.info('Google login is coming soon!');
    }
}
