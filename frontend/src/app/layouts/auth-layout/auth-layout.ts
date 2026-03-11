import { Component, inject } from '@angular/core';
import { ThemeService } from '../../core/services/theme-service';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-auth-layout',
    imports: [RouterOutlet],
    templateUrl: './auth-layout.html',
    styles: ``,
})
export class AuthLayout {
    protected readonly themeService = inject(ThemeService);
}
