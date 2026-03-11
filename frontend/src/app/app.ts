import { Component, inject, PLATFORM_ID, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/services/theme-service';
import { NgxSonnerToaster } from 'ngx-sonner';
import { isPlatformBrowser } from '@angular/common';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, NgxSonnerToaster],
    templateUrl: './app.html',
    styleUrl: './app.scss',
})
export class App {
    isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
    private themeService = inject(ThemeService);
    protected isDark = this.themeService.isDark;
}
