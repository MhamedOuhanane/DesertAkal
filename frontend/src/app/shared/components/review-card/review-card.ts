import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { Review } from '../../../core/models/review.model';

@Component({
    selector: 'app-review-card',
    standalone: true,
    imports: [DatePipe, MatIcon],
    template: `
        <div class="rounded-xl border border-border p-4 transition-all hover:border-primary/20">
            <div class="mb-2 flex items-start justify-between">
                <div class="flex items-center gap-2">
                    @if (showAuthor()) {
                        <div
                            class="flex h-7 w-7 items-center justify-center rounded-full bg-primary/10 text-[9px] font-bold text-primary"
                        >
                            {{ review().touristName.charAt(0) || '?' }}
                        </div>
                        <span class="text-xs font-semibold text-text-primary">{{
                            review().touristName
                        }}</span>
                    }
                    <span
                        class="rounded-full px-1.5 py-0.5 text-[9px] font-semibold"
                        [class]="
                            review().reviewableType === 'TOUR'
                                ? 'bg-blue-500/10 text-blue-600'
                                : 'bg-purple-500/10 text-purple-600'
                        "
                    >
                        {{ review().reviewableType }}
                    </span>
                </div>
                <div class="flex items-center gap-0.5">
                    @for (_ of [1, 2, 3, 4, 5]; track $index) {
                        <mat-icon
                            style="font-size: 13px; width: 13px; height: 13px"
                            [class.text-warning]="$index < review().rating"
                            [class.text-border]="$index >= review().rating"
                            >star</mat-icon
                        >
                    }
                </div>
            </div>
            @if (review().reviewableName) {
                <p class="mb-1 text-[11px] text-text-tertiary">
                    on
                    <span class="font-medium text-text-secondary">{{
                        review().reviewableName
                    }}</span>
                </p>
            }
            <p class="text-sm text-text-secondary">{{ review().comment }}</p>
            <p class="mt-2 text-[10px] text-text-tertiary">
                {{ review().createdAt | date: 'MMM d, y' }}
            </p>

            @if (showActions()) {
                <div class="mt-2 flex items-center gap-2 border-t border-border pt-2">
                    <ng-content select="[actions]" />
                </div>
            }
        </div>
    `,
})
export class ReviewCard {
    review = input.required<Review>();
    showAuthor = input(true);
    showActions = input(false);
}
