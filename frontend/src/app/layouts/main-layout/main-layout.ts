import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PublicHeader } from '../../shared/components/public-header/public-header';
import { Footer } from '../../shared/components/footer/footer';

@Component({
    selector: 'app-main-layout',
    imports: [RouterOutlet, PublicHeader, Footer],
    host: {
        class: 'block',
    },
    template: `
        <div class="flex min-h-screen flex-col bg-main-bg">
            <app-public-header />

            <main class="flex-1">
                <router-outlet />
            </main>

            <app-footer />
        </div>
    `,
    styles: ``,
})
export class MainLayout {}
