import { Component, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';
import { TourFind } from '../../../../../../core/models/tour.model';
import { CurrencyPipe } from '@angular/common';

@Component({
    selector: 'app-step-trip-details',
    imports: [ReactiveFormsModule, CurrencyPipe, MatIcon],
    templateUrl: './step-trip-details.html',
})
export class StepTripDetails {
    form = input.required<FormGroup>();
    tour = input.required<TourFind>();
    minDate = input.required<string>();
    pricePerPerson = input.required<number>();

    dateChanged = output<{ startDate: string; endDate: string }>();
    amountChanged = output<void>();
    next = output<void>();

    onDateChange(): void {
        const startDate = this.form().get('startDate')?.value;
        if (!startDate) return;

        const endDate = this.computeEndDate();
        if (endDate) {
            this.dateChanged.emit({
                startDate: new Date(startDate).toISOString(),
                endDate: new Date(endDate).toISOString(),
            });
        }
    }

    onPeopleChange(): void {
        this.amountChanged.emit();
    }

    incrementPeople(): void {
        const current = this.form().get('numberPeople')?.value || 1;
        if (current < 20) {
            this.form().patchValue({ numberPeople: current + 1 });
            this.amountChanged.emit();
        }
    }

    decrementPeople(): void {
        const current = this.form().get('numberPeople')?.value || 1;
        if (current > 1) {
            this.form().patchValue({ numberPeople: current - 1 });
            this.amountChanged.emit();
        }
    }

    computeEndDate(): string {
        const start = this.form().get('startDate')?.value;
        const dur = this.tour().durationDays || 0;
        if (!start || !dur) return '';
        const d = new Date(start);
        d.setDate(d.getDate() + dur);
        return d.toISOString().split('T')[0];
    }

    isValid(): boolean {
        const f = this.form();
        return (
            !!f.get('startDate')?.value &&
            f.get('numberPeople')?.valid === true &&
            f.get('amount')?.value > 0
        );
    }

    onNext(): void {
        if (this.isValid()) {
            this.next.emit();
        }
    }

    getError(field: string): string {
        const c = this.form().get(field);
        if (!c?.touched || !c?.invalid) return '';
        if (c.hasError('required')) return 'Required';
        if (c.hasError('min')) return 'Value too low';
        if (c.hasError('max')) return 'Maximum 20 people';
        return '';
    }
}
