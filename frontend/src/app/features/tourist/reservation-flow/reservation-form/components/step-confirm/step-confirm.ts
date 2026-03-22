import { Component, input, output } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { TourFind } from '../../../../../../core/models/tour.model';
import { Guide } from '../../../../../../core/models/guide.model';

@Component({
    selector: 'app-step-confirm',
    imports: [CurrencyPipe, MatIcon],
    templateUrl: './step-confirm.html',
})
export class StepConfirm {
    form = input.required<FormGroup>();
    tour = input.required<TourFind>();
    selectedGuide = input.required<Guide | null>();
    endDate = input.required<string>();
    isSubmitting = input(false);

    back = output<void>();
    confirm = output<void>();
}
