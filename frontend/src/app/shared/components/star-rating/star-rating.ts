import { Component, computed, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MatIcon } from '@angular/material/icon';

@Component({
    selector: 'app-star-rating',
    imports: [DecimalPipe, MatIcon],
    template: `
        <div
            class="flex items-center gap-1"
            [class.gap-0.5]="size() !== 'sm'"
            [class.gap-px]="size() === 'sm'"
        >
            @for (state of stars(); track $index) {
                <mat-icon
                    [style.font-size]="iconSize() + 'px'"
                    [style.width]="iconSize() + 'px'"
                    [style.height]="iconSize() + 'px'"
                    [class.star-filled]="state !== 'empty'"
                    [class.star-empty]="state === 'empty'"
                >
                    @switch (state) {
                        @case ('full') {
                            star
                        }
                        @case ('half') {
                            star_half
                        }
                        @case ('empty') {
                            star_outline
                        }
                    }
                </mat-icon>
            }

            @if (showValue()) {
                <span
                    class="font-bold"
                    [class.ml-1.5]="size() !== 'sm'"
                    [class.ml-1]="size() === 'sm'"
                    [class.text-sm]="size() === 'md' || size() === 'lg'"
                    [class.text-xs]="size() === 'sm'"
                    [class]="ratingColor()"
                >
                    {{ rating() | number: '1.1-1' }}
                </span>
            }

            @if (showCount() && count() !== undefined) {
                <span
                    class="text-text-tertiary"
                    [class.ml-1]="true"
                    [class.text-xs]="size() === 'md' || size() === 'lg'"
                    [class.text-[10px]]="size() === 'sm'"
                >
                    ({{ count() }})
                </span>
            }
        </div>
    `,
})
export class StarRating {
    rating = input.required<number>();
    maxStars = input(5);
    size = input<'sm' | 'md' | 'lg'>('md');
    showValue = input(true);
    showCount = input(false);
    count = input<number>();

    stars = computed(() => {
        const r = this.rating();
        return Array.from({ length: this.maxStars() }, (_, i) => {
            const starValue = i + 1;
            if (r >= starValue) return 'full';
            if (r >= starValue - 0.5) return 'half';
            return 'empty';
        });
    });

    iconSize = computed(() => {
        switch (this.size()) {
            case 'sm':
                return 12;
            case 'lg':
                return 20;
            default:
                return 14;
        }
    });

    ratingColor = computed(() => {
        const r = this.rating();
        if (r >= 4) return 'text-green-600';
        if (r >= 3) return 'text-primary';
        if (r >= 2) return 'text-warning';
        return 'text-red-500';
    });
}
