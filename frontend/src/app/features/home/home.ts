import { Component } from '@angular/core';

@Component({
    selector: 'app-home',
    imports: [],
    templateUrl: './home.html',
    styleUrl: './home.scss',
})
export class Home {
    cards = [
        {
            icon: '🐪',
            title: 'Desert Trek',
            description:
                'An unforgettable experience in the heart of the Moroccan Sahara with expert local guides.',
            badge: 'Popular',
        },
        {
            icon: '⭐',
            title: 'Starlight Night',
            description:
                'Sleep under the stars in a traditional luxury camp with panoramic desert views.',
            badge: 'Exclusive',
        },
        {
            icon: '🏕️',
            title: 'Luxury Camp',
            description:
                'Enjoy modern comfort in the middle of the golden dunes with premium amenities.',
            badge: 'Premium',
        },
    ];

    stats = [
        { value: '500+', label: 'Happy Travelers' },
        { value: '50+', label: 'Unique Tours' },
        { value: '15+', label: 'Expert Guides' },
        { value: '4.9', label: 'Average Rating' },
    ];
}
