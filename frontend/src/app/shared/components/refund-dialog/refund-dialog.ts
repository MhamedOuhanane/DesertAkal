import { Component, inject, input, output, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { Payment } from '../../../core/models/payment.model';

@Component({
    selector: 'app-refund-dialog',
    imports: [ReactiveFormsModule, CurrencyPipe, MatIcon],
    templateUrl: './refund-dialog.html',
})
export class RefundDialog implements OnInit {
    private fb = inject(FormBuilder);

    payment = input.required<Payment>();
    isSubmitting = input(false);

    confirm = output<{ type: 'full' | 'partial'; amount?: number }>();
    cancel = output<void>();

    form!: FormGroup;
    refundType = signal<'full' | 'partial'>('full');

    ngOnInit(): void {
        this.form = this.fb.group({
            amount: [
                null,
                [Validators.required, Validators.min(0.01), Validators.max(this.payment().amount)],
            ],
        });
    }

    isValid(): boolean {
        if (this.refundType() === 'full') return true;
        return this.form.valid;
    }

    onSubmit(): void {
        if (this.refundType() === 'full') {
            this.confirm.emit({ type: 'full' });
        } else {
            if (this.form.invalid) {
                this.form.markAllAsTouched();
                return;
            }
            this.confirm.emit({
                type: 'partial',
                amount: this.form.value.amount,
            });
        }
    }
}
