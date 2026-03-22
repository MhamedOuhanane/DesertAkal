import { Component, input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { DecimalPipe } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { TourFind } from '../../../../../../core/models/tour.model';
import { Guide } from '../../../../../../core/models/guide.model';

@Component({
    selector: 'app-booking-sidebar',
    standalone: true,
    imports: [CurrencyPipe, DecimalPipe, MatIcon],
    templateUrl: './reservation-sidebar.html',
})
export class BookingSidebar {
    tour = input.required<TourFind>();
    form = input.required<FormGroup>();
    selectedGuide = input.required<Guide | null>();
    endDate = input.required<string>();
    currentStep = input.required<number>();
    pricePerPerson = input.required<number>();

    getStepLabel(): string {
        const labels: Record<number, string> = {
            1: 'Trip Details',
            2: 'Select Guide',
            3: 'Confirmation',
        };
        return labels[this.currentStep()] || '';
    }
}
