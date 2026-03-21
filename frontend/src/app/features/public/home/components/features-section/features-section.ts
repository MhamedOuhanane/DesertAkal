import { Component } from '@angular/core';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-features-section',
    imports: [MatIcon],
    templateUrl: './features-section.html',
})
export class FeaturesSection {
    readonly features = [
        {
            icon: 'verified_user',
            title: 'Certified Guides',
            desc: "Verified professionals with deep knowledge of Morocco's history, culture, and hidden gems.",
            accent: 'bg-primary/10 text-primary',
        },
        {
            icon: 'lock',
            title: 'Secure Booking',
            desc: 'Safe payment processing with instant confirmation and QR code vouchers for every tour.',
            accent: 'bg-success/10 text-success',
        },
        {
            icon: 'headset_mic',
            title: '24/7 Support',
            desc: 'Our team is available around the clock to help you before and during your trip.',
            accent: 'bg-info/10 text-info',
        },
        {
            icon: 'diamond',
            title: 'Authentic Experiences',
            desc: 'Carefully curated tours that go beyond tourist spots to reveal the real Morocco.',
            accent: 'bg-warning/10 text-warning',
        },
    ];
}
