import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/services/theme-service';
import { NgxSonnerToaster } from 'ngx-sonner';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, NgxSonnerToaster],
    templateUrl: './app.html',
    styleUrl: './app.scss',
})
export class App {
    private themeService = inject(ThemeService);
    protected isDark = this.themeService.isDark;
}
