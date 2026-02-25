import { isPlatformBrowser } from '@angular/common';
import { effect, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class ThemeService {
    private platformId = inject(PLATFORM_ID);

    isDark = signal<boolean>(false);

    constructor() {
        if (isPlatformBrowser(this.platformId)) {
            const saved = localStorage.getItem('is-dark') === 'true';
            this.isDark.set(saved);
        }

        effect(() => {
            const dark = this.isDark();
            if (isPlatformBrowser(this.platformId)) {
                const root = document.documentElement;
                if (dark) {
                    root.classList.add('dark');
                    root.classList.add('dark-mode');
                } else {
                    root.classList.remove('dark');
                    root.classList.remove('dark-mode');
                }

                localStorage.setItem('is-dark', String(dark));
            }
        });
    }

    toggleTheme() {
        this.isDark.update((v) => !v);
    }
}
