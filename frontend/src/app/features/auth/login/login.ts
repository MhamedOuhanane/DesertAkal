import { Component, effect, inject, PLATFORM_ID, signal, untracked } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LoginRequest } from '../../../core/auth/auth.models';
import { BrandLogo } from "../../../shared/components/brand-logo/brand-logo";
import { ScreenService } from '../../../core/services/screen-service';
import { toast } from 'ngx-sonner';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, BrandLogo],
    templateUrl: './login.html',
})
export class Login {
    private fb = inject(FormBuilder);
    protected readonly screenService = inject(ScreenService);
    protected readonly authStore = inject(AuthStore);

    isLoading = this.authStore.loading;

    showPassword = signal(false);

    loginForm = this.fb.nonNullable.group({
        username: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
    });

    togglePassword(): void {
        this.showPassword.update((v) => !v);
    }

    onSubmit(): void {
        if (this.loginForm.invalid) {
            this.loginForm.markAllAsTouched();
            return;
        }

        const credentials = this.loginForm.getRawValue();
        this.authStore.login(credentials);
    }

    loginWithGoogle(): void {
        toast.info('Google login is coming soon!');
    }
}