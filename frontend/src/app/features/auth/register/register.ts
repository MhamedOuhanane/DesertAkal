import { Component, inject, signal } from '@angular/core';
import {
    AbstractControl,
    FormBuilder,
    ReactiveFormsModule,
    ValidationErrors,
    Validators,
} from '@angular/forms';
import { Register as RegisterModel } from '../../../core/auth/auth.models';
import { RouterLink, Router } from '@angular/router';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';
import { TextInput } from '../../../shared/components/text-input/text-input';
import { ScreenService } from '../../../core/services/screen-service';
import { AuthService } from '../../../core/auth/auth-service';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment.development';
import { OauthLogin } from '../../../shared/components/oauth-login/oauth-login';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, BrandLogo, TextInput, OauthLogin],
    templateUrl: './register.html',
})
export class Register {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private router = inject(Router);

    protected readonly screenService = inject(ScreenService);

    isLoading = signal(false);
    errorMessage = signal<string | null>(null);

    registerForm = this.fb.nonNullable.group(
        {
            firstName: [
                '',
                [Validators.required, Validators.minLength(3), Validators.maxLength(50)],
            ],
            lastName: [
                '',
                [Validators.required, Validators.minLength(3), Validators.maxLength(50)],
            ],
            username: [
                '',
                [Validators.required, Validators.minLength(7), Validators.maxLength(30)],
            ],
            email: ['', [Validators.required, Validators.email]],
            password: [
                '',
                [Validators.required, Validators.minLength(8), Validators.maxLength(100)],
            ],
            confirmPassword: ['', [Validators.required]],
            terms: [false, [Validators.requiredTrue]],
        },
        {
            validators: [Register.passwordsMatch],
        },
    );

    static passwordsMatch(group: AbstractControl): ValidationErrors | null {
        const password = group.get('password')?.value;
        const confirm = group.get('confirmPassword')?.value;

        if (password && confirm && password !== confirm) {
            group.get('confirmPassword')?.setErrors({
                mismatch: true,
            });
            return { mismatch: true };
        }

        const confirmCtrl = group.get('confirmPassword');
        if (confirmCtrl?.hasError('mismatch')) {
            confirmCtrl.setErrors(null);
        }

        return null;
    }

    async onSubmit(): Promise<void> {
        this.errorMessage.set(null);

        if (this.registerForm.invalid) {
            this.registerForm.markAllAsTouched();
            return;
        }

        this.isLoading.set(true);

        try {
            const formValue = this.registerForm.getRawValue();

            const payload: RegisterModel = {
                firstName: formValue.firstName,
                lastName: formValue.lastName,
                username: formValue.username,
                email: formValue.email,
                password: formValue.password,
                confirmPassword: formValue.confirmPassword,
                roleUuid: environment.touristRole,
            };

            const response = await firstValueFrom(this.authService.register(payload));

            toast.success('Account created!', {
                description: response.message || 'Please check your email to verify your account.',
                duration: 6000,
            });

            this.router.navigate(['/auth/login'], {
                queryParams: { registered: 'true' },
            });
        } catch (error: any) {
            console.log(error);
            if (error.status === 400 && error.error?.error) {
                const serverErrors = error.error.error;

                Object.keys(serverErrors).forEach((key) => {
                    const control = this.registerForm.get(key);
                    if (control) {
                        control.setErrors({ serverError: serverErrors[key] });
                    }
                });
            }

            const msg = error?.error?.message || 'Registration failed. Please try again.';
            this.errorMessage.set(msg);
            toast.error(msg);
        } finally {
            this.isLoading.set(false);
        }
    }

    registerWithGoogle(): void {
        toast.info('Google sign-up is coming soon!');
    }

    registerWithFacebook(): void {
        toast.info('Facebook sign-up is coming soon!');
    }
}
