import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-brand-logo',
    imports: [RouterLink],
    template: `
        <a [routerLink]="link()" class="flex items-center gap-2.5">
            <img src="favicon.svg" alt="DesertAkal" [class]="classLogo()" />
            @if (isExpanded()) {
                <span class="text-xl font-bold tracking-tight text-logo-blue">
                    Desert<span class="text-primary">Akal</span>
                </span>
            }
        </a>
    `,
    styles: ``,
})
export class BrandLogo {
    readonly link = input<string>('/');
    readonly classLogo = input<string>('h-16 w-auto');
    readonly isExpanded = input<boolean>(true);
}
