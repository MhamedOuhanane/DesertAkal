import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-hero-section',
    imports: [RouterLink, MatIcon],
    templateUrl: './hero-section.html',
})
export class HeroSection {
    readonly highlights = [
        { icon: 'verified', value: '4.9★', label: 'Rating' },
        { icon: 'groups', value: '120+', label: 'Guides' },
        { icon: 'tour', value: '500+', label: 'Tours' },
    ];
}
