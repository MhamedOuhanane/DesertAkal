import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIcon } from '@angular/material/icon';
import { toast } from 'ngx-sonner';

@Component({
    selector: 'app-contact',
    imports: [ReactiveFormsModule, MatIcon],
    templateUrl: './contact.html',
})
export class Contact {
    form: FormGroup;
    isSubmitting = signal(false);
    isSubmitted = signal(false);

    readonly contactInfo = [
        {
            icon: 'location_on',
            title: 'Address',
            lines: ['Merzouga, Errachidia Province', 'Morocco'],
            color: 'bg-primary/10 text-primary',
        },
        {
            icon: 'email',
            title: 'Email',
            lines: ['contact@desertakal.com', 'support@desertakal.com'],
            color: 'bg-info/10 text-info',
        },
        {
            icon: 'phone',
            title: 'Phone',
            lines: ['+212 6 00 00 00 00', 'Mon – Fri, 9am – 6pm'],
            color: 'bg-success/10 text-success',
        },
        {
            icon: 'schedule',
            title: 'Hours',
            lines: ['Monday – Friday: 9am – 6pm', 'Saturday: 10am – 2pm'],
            color: 'bg-warning/10 text-warning',
        },
    ];

    constructor(private fb: FormBuilder) {
        this.form = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(2)]],
            email: ['', [Validators.required, Validators.email]],
            subject: ['', [Validators.required, Validators.minLength(3)]],
            message: [
                '',
                [Validators.required, Validators.minLength(10), Validators.maxLength(2000)],
            ],
        });
    }

    async onSubmit(): Promise<void> {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.isSubmitting.set(true);
        await new Promise((r) => setTimeout(r, 1500));
        this.isSubmitting.set(false);
        this.isSubmitted.set(true);
        this.form.reset();
        toast.success('Message sent! We will get back to you soon.');
    }

    getError(field: string): string {
        const c = this.form.get(field);
        if (!c?.touched || !c?.invalid) return '';
        if (c.hasError('required')) return `${field} is required`;
        if (c.hasError('email')) return 'Invalid email';
        if (c.hasError('minlength')) return 'Too short';
        return '';
    }
}
