import { Component, inject } from '@angular/core';
import { ThemeService } from '../../../core/services/theme-service';

@Component({
    selector: 'app-theme-toggle',
    imports: [],
    template: `
        <button
            (click)="themeService.toggleTheme()"
            class="flex h-10 w-10 items-center justify-center rounded-lg text-text-secondary transition-all duration-200 hover:bg-primary/5 hover:text-primary"
        >
            <span class="material-icons cursor-pointer text-[20px]">
                {{ themeService.isDark() ? 'light_mode' : 'dark_mode' }}
            </span>
        </button>
    `,
    styles: ``,
})
export class ThemeToggle {
    readonly themeService = inject(ThemeService);
}
