import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { ScreenService } from '../../../core/services/screen-service';

@Component({
    selector: 'app-about',
    imports: [RouterLink, MatIcon],
    templateUrl: './about.html',
})
export class About {
    protected readonly screenService = inject(ScreenService);
    readonly values = [
        {
            icon: 'diversity_3',
            title: 'Community First',
            desc: 'We empower local guides across Morocco and connect them with travelers seeking authentic experiences.',
            color: 'bg-primary/10 text-primary',
        },
        {
            icon: 'eco',
            title: 'Sustainable Tourism',
            desc: 'Every tour respects local ecosystems, cultural heritage, and traditional communities from Tangier to the Sahara.',
            color: 'bg-success/10 text-success',
        },
        {
            icon: 'handshake',
            title: 'Trust & Transparency',
            desc: 'Verified guides, honest reviews, and secure bookings — no hidden fees, no surprises.',
            color: 'bg-info/10 text-info',
        },
        {
            icon: 'auto_awesome',
            title: 'Unforgettable Moments',
            desc: 'We go beyond sightseeing to create stories you will tell forever — from mountain peaks to ocean sunsets.',
            color: 'bg-warning/10 text-warning',
        },
    ];
}
