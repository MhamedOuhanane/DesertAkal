import { Component, input } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { HomeReview } from '../../../../../core/models/review.model';

@Component({
    selector: 'app-reviews-section',
    imports: [MatIcon],
    templateUrl: './reviews-section.html',
})
export class ReviewsSection {
    reviews = input<HomeReview[]>([]);
    loading = input(true);
}
