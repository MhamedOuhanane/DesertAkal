import { Component, DestroyRef, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth-service';
import { firstValueFrom } from 'rxjs';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';

type ConfirmState = 'loading' | 'success' | 'error';

@Component({
    selector: 'app-confirm-email',
    imports: [RouterLink, BrandLogo],
    templateUrl: './confirm-email.html',
})
export class ConfirmEmail implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private authService = inject(AuthService);
    private destroyRef = inject(DestroyRef);
    private platformId = inject(PLATFORM_ID);

    state = signal<ConfirmState>('loading');
    errorMessage = signal('');
    redirectCountdown = signal(5);

    private intervalId: ReturnType<typeof setInterval> | null = null;

    constructor() {
        this.destroyRef.onDestroy(() => {
            if (this.intervalId) clearInterval(this.intervalId);
        });
    }

    ngOnInit(): void {
        if (!isPlatformBrowser(this.platformId)) return;

        const token = this.route.snapshot.queryParamMap.get('token');

        if (!token) {
            this.state.set('error');
            this.errorMessage.set('No verification token found in the URL.');
            return;
        }

        this.verifyToken(token);
    }

    private async verifyToken(token: string): Promise<void> {
        try {
            await firstValueFrom(this.authService.confirmEmail(token));
            this.state.set('success');
            this.startRedirectCountdown();
        } catch (error: any) {
            this.state.set('error');
            this.errorMessage.set(
                error?.error?.message || 'This verification link is invalid or has expired.',
            );
        }
    }

    private startRedirectCountdown(): void {
        this.redirectCountdown.set(5);

        this.intervalId = setInterval(() => {
            this.redirectCountdown.update((v) => {
                if (v <= 1) {
                    if (this.intervalId) clearInterval(this.intervalId);
                    this.router.navigate(['/auth/login'], { replaceUrl: true });
                    return 0;
                }
                return v - 1;
            });
        }, 1000);
    }

    goToLogin(): void {
        if (this.intervalId) clearInterval(this.intervalId);
        this.router.navigate(['/auth/login'], { replaceUrl: true });
    }
}
