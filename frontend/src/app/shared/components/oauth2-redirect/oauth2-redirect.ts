import { Component, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth-service'; // تأكد من المسار
import { toast } from 'ngx-sonner';
import { AuthStore } from '../../../core/auth/auth.store';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-oauth2-redirect',
  standalone: true,
  template: `
    <div class="flex h-screen flex-col items-center justify-center bg-main-bg">
      <div class="h-12 w-12 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
      <p class="mt-4 text-text-secondary animate-pulse">Completing secure login...</p>
    </div>
  `
})
export class OAuth2RedirectComponent implements OnInit {
  private route = inject(ActivatedRoute);
    private router = inject(Router);
    private authStore = inject(AuthStore);
    private platformId = inject(PLATFORM_ID);

    ngOnInit(): void {
        if (!isPlatformBrowser(this.platformId)) return;

        const params = this.route.snapshot.queryParams;

        const token = params['token'];

        if (!token) {
            toast.error('OAuth login failed — no token received.');
            this.router.navigate(['/auth/login']);
            return;
        }

        const oauthData = {
            token: params['token'],
            userUuid: params['userUuid'] || params['uuid'] || '',
            username: params['username'] || params['email'] || '',
            fullName: params['fullName'] || params['name'] || '',
            role: params['role'] || 'TOURIST',
            photo: params['photo'] || '',
        };

        try {
            this.authStore.handleOAuthSuccess(oauthData);

            toast.success(`Welcome back, ${oauthData.fullName || oauthData.username}!`);

            setTimeout(() => {
                this.router.navigate(['/'], { replaceUrl: true });
            }, 100);
        } catch (error) {
            toast.error('Something went wrong during login.');
            this.router.navigate(['/auth/login']);
        }
    }
}