import { Component, DestroyRef, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth-service';
import { toast } from 'ngx-sonner';
import { firstValueFrom } from 'rxjs';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';

@Component({
    selector: 'app-verify-email',
    imports: [RouterLink, ReactiveFormsModule, BrandLogo],
    templateUrl: './verify-email.html',
})
export class VerifyEmail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private destroyRef = inject(DestroyRef);
    private platformId = inject(PLATFORM_ID);

    email = signal<string | null>(null);
    countdown = signal(0);
    resendCount = signal(0);
    isResending = signal(false);
    maxResends = 5;

    private intervalId: ReturnType<typeof setInterval> | null = null;

    emailForm = this.fb.nonNullable.group({
        email: ['', [Validators.required, Validators.email]],
    });

    constructor() {
        this.destroyRef.onDestroy(() => {
            if (this.intervalId) clearInterval(this.intervalId);
        });
    }

    ngOnInit(): void {
        if (!isPlatformBrowser(this.platformId)) return;

        const paramEmail =
            this.route.snapshot.queryParamMap.get('email') || history.state?.email || null;

        if (paramEmail) {
            this.email.set(paramEmail);
            this.startCountdown();
        }
    }

    submitEmail(): void {
        if (this.emailForm.invalid) {
            this.emailForm.markAllAsTouched();
            return;
        }
        this.email.set(this.emailForm.getRawValue().email);
        this.resend();
    }

    async resend(): Promise<void> {
        const currentEmail = this.email();
        if (!currentEmail || this.countdown() > 0 || this.isResending()) return;
        if (this.resendCount() >= this.maxResends) {
            toast.error('Maximum resend limit reached. Please contact support.');
            return;
        }

        this.isResending.set(true);

        try {
            await firstValueFrom(this.authService.resendVerificationEmail(currentEmail));
            this.resendCount.update((c) => c + 1);
            toast.success('Verification email sent!');
            this.startCountdown();
        } catch (error: any) {
            const msg = error?.error?.message || 'Failed to send verification email.';
            toast.error(msg);
        } finally {
            this.isResending.set(false);
        }
    }

    private startCountdown(): void {
        if (this.intervalId) clearInterval(this.intervalId);

        this.countdown.set(60);

        this.intervalId = setInterval(() => {
            this.countdown.update((v) => {
                if (v <= 1) {
                    if (this.intervalId) clearInterval(this.intervalId);
                    return 0;
                }
                return v - 1;
            });
        }, 1000);
    }

    get formattedCountdown(): string {
        const s = this.countdown();
        return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
    }

    get progressPercent(): number {
        return ((60 - this.countdown()) / 60) * 100;
    }
}
