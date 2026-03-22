import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { toast } from 'ngx-sonner';
import { MatIcon } from '@angular/material/icon';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReviewService } from '../../../core/services/review-service';
import { ReviewCreate, ReviewFormData } from '../../../core/models/review.model';

@Component({
    selector: 'app-review-form-dialog',
    imports: [ReactiveFormsModule, MatIcon],
    templateUrl: './review-form.html',
})
export class ReviewFormDialog {
    private dialogRef = inject(MatDialogRef<ReviewFormDialog>);
    data: ReviewFormData = inject(MAT_DIALOG_DATA);
    private reviewService = inject(ReviewService);
    private fb = inject(FormBuilder);

    form: FormGroup;
    isSubmitting = signal(false);
    hoverRating = signal(0);

    constructor() {
        this.form = this.fb.group({
            rating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
            comment: [
                '',
                [Validators.required, Validators.minLength(10), Validators.maxLength(1000)],
            ],
        });
    }

    setRating(rating: number): void {
        this.form.patchValue({ rating });
    }

    getRatingLabel(): string {
        const r = this.hoverRating() || this.form.get('rating')?.value || 0;
        const labels: Record<number, string> = {
            1: '😞 Poor',
            2: '😐 Fair',
            3: '🙂 Good',
            4: '😊 Very Good',
            5: '🤩 Excellent!',
        };
        return labels[r] || 'Select a rating';
    }

    async onSubmit(): Promise<void> {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.isSubmitting.set(true);
        try {
            const dto: ReviewCreate = {
                rating: this.form.value.rating,
                comment: this.form.value.comment,
                reviewableUuid: this.data.reviewableUuid,
                reviewableType: this.data.reviewableType,
            };
            await firstValueFrom(this.reviewService.create(dto));
            toast.success('Review posted! Thank you for your feedback.');
            this.dialogRef.close(true);
        } catch (err: any) {
            toast.error(err?.error?.message || 'Failed to post review');
        } finally {
            this.isSubmitting.set(false);
        }
    }

    close(): void {
        this.dialogRef.close(false);
    }
}
