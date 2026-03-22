import { Component, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';
import { Guide } from '../../../../../../core/models/guide.model';

@Component({
    selector: 'app-step-select-guide',
    imports: [ReactiveFormsModule, MatIcon],
    templateUrl: './step-select-guide.html',
})
export class StepSelectGuide {
    form = input.required<FormGroup>();
    guides = input.required<Guide[]>();
    isLoading = input(false);

    back = output<void>();
    next = output<void>();

    selectGuide(uuid: string): void {
        this.form().patchValue({ guideUuid: uuid });
    }

    isSelected(uuid: string): boolean {
        return this.form().get('guideUuid')?.value === uuid;
    }

    hasSelection(): boolean {
        return !!this.form().get('guideUuid')?.value;
    }

    onNext(): void {
        if (this.hasSelection()) {
            this.next.emit();
        }
    }
}
